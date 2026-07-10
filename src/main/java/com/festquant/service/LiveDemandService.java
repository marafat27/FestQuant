package com.festquant.service;

import com.festquant.domain.Event;
import com.festquant.domain.PriceRecommendation;
import com.festquant.stream.DemandScenario;
import com.festquant.stream.LiveDemandSnapshot;
import com.festquant.stream.LivePricingPolicy;
import com.festquant.stream.LiveTicketSale;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compressed-time demand feed using a producer-consumer pipeline. A scheduled
 * producer simulates clean ticket activity; a dedicated consumer updates
 * rolling demand, computes bounded live prices and notifies SSE observers.
 */
@Service
public final class LiveDemandService {
    private static final int WINDOW_SIZE = 8;
    private static final int INTERVAL_SECONDS = 2;

    private final AnalyticsFacade analytics;
    private final LivePricingPolicy pricingPolicy = new LivePricingPolicy();
    private final BlockingQueue<LiveTicketSale> queue = new ArrayBlockingQueue<>(2048);
    private final Map<String, Event> eventsById = new LinkedHashMap<>();
    private final Map<String, Double> historicalPrices = new ConcurrentHashMap<>();
    private final Map<String, Double> currentPrices = new ConcurrentHashMap<>();
    private final Map<String, Double> previousRollingDemand = new ConcurrentHashMap<>();
    private final Map<String, Deque<Integer>> demandWindows = new ConcurrentHashMap<>();
    private final Map<String, LiveDemandSnapshot> snapshots = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService producer =
            Executors.newSingleThreadScheduledExecutor(namedThread("festquant-live-producer"));
    private final ExecutorService consumer =
            Executors.newSingleThreadExecutor(namedThread("festquant-live-consumer"));
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicLong sequence = new AtomicLong();
    private final Random random = new Random(42L);
    private volatile DemandScenario scenario = DemandScenario.NORMAL;

    public LiveDemandService(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @PostConstruct
    public void initialise() {
        analytics.events().forEach(event -> eventsById.put(event.getEventId(), event));
        analytics.recommendations().forEach(recommendation ->
                historicalPrices.put(recommendation.getEventId(), recommendation.getRecommendedPrice()));

        Instant now = Instant.now();
        for (Event event : eventsById.values()) {
            double startingPrice = historicalPrices.getOrDefault(event.getEventId(), event.getBasePrice());
            currentPrices.put(event.getEventId(), startingPrice);
            demandWindows.put(event.getEventId(), new ArrayDeque<>());
            snapshots.put(event.getEventId(), new LiveDemandSnapshot(
                    event.getEventId(), event.getEventName(), now, 0, 0,
                    0, 0, 0, startingPrice, event.getBasePrice(),
                    percentChange(startingPrice, event.getBasePrice()), "STABLE",
                    scenario.name(), true
            ));
        }

        consumer.submit(this::consumeContinuously);
        producer.scheduleAtFixedRate(this::produceBatch, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public List<LiveDemandSnapshot> snapshots() {
        return snapshots.values().stream()
                .sorted(Comparator.comparing(LiveDemandSnapshot::eventName))
                .toList();
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("snapshot").data(snapshots()));
        } catch (IOException exception) {
            emitters.remove(emitter);
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    public Map<String, Object> start() {
        running.set(true);
        publishControlState();
        return status();
    }

    public Map<String, Object> pause() {
        running.set(false);
        publishControlState();
        return status();
    }

    public Map<String, Object> useScenario(DemandScenario selectedScenario) {
        scenario = selectedScenario;
        running.set(true);
        publishControlState();
        return status();
    }

    public Map<String, Object> status() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("running", running.get());
        state.put("scenario", scenario.name());
        state.put("intervalSeconds", INTERVAL_SECONDS);
        state.put("queueDepth", queue.size());
        return state;
    }

    private void produceBatch() {
        if (!running.get() || shutdown.get()) {
            return;
        }

        long tick = sequence.incrementAndGet();
        Instant observedAt = Instant.now();
        for (Event event : eventsById.values()) {
            double expected = expectedDemand(event);
            double wave = 0.88 + 0.20 * Math.sin((tick + Math.abs(event.getEventId().hashCode() % 7)) / 2.6);
            double noise = 0.76 + random.nextDouble() * 0.48;
            int tickets = Math.max(0, (int) Math.round(expected * scenario.multiplier() * wave * noise));
            int views = Math.max(15, tickets * (18 + random.nextInt(15)) + random.nextInt(90));
            int wishlists = Math.max(1, (int) Math.round(views * (0.08 + random.nextDouble() * 0.12)));
            queue.offer(new LiveTicketSale(
                    event.getEventId(), observedAt, tick, views, wishlists, tickets
            ));
        }
    }

    private void consumeContinuously() {
        while (!shutdown.get()) {
            try {
                process(queue.take());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException exception) {
                // One malformed update must not stop the long-lived consumer.
                System.err.println("Live demand update skipped: " + exception.getMessage());
            }
        }
    }

    private void process(LiveTicketSale sale) {
        Event event = eventsById.get(sale.eventId());
        if (event == null) {
            return;
        }

        Deque<Integer> window = demandWindows.get(sale.eventId());
        window.addLast(sale.ticketsSold());
        while (window.size() > WINDOW_SIZE) {
            window.removeFirst();
        }

        double rollingDemand = window.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double previousDemand = previousRollingDemand.getOrDefault(sale.eventId(), rollingDemand);
        previousRollingDemand.put(sale.eventId(), rollingDemand);
        double demandIndex = rollingDemand / expectedDemand(event);

        double historicalPrice = historicalPrices.getOrDefault(sale.eventId(), event.getBasePrice());
        double previousPrice = currentPrices.getOrDefault(sale.eventId(), historicalPrice);
        double targetPrice = pricingPolicy.recommend(event, historicalPrice, demandIndex);
        double livePrice = Math.round((previousPrice * 0.55 + targetPrice * 0.45) / 10.0) * 10.0;
        currentPrices.put(sale.eventId(), livePrice);

        LiveDemandSnapshot snapshot = new LiveDemandSnapshot(
                event.getEventId(),
                event.getEventName(),
                sale.observedAt(),
                sale.sequence(),
                sale.ticketsSold(),
                round(rollingDemand),
                sale.views(),
                sale.wishlists(),
                livePrice,
                event.getBasePrice(),
                percentChange(livePrice, event.getBasePrice()),
                trend(previousDemand, rollingDemand),
                scenario.name(),
                running.get()
        );
        snapshots.put(sale.eventId(), snapshot);
        broadcast("demand-update", snapshot);
    }

    private void publishControlState() {
        List<LiveDemandSnapshot> updated = new ArrayList<>();
        for (LiveDemandSnapshot snapshot : snapshots()) {
            LiveDemandSnapshot current = new LiveDemandSnapshot(
                    snapshot.eventId(), snapshot.eventName(), Instant.now(), snapshot.sequence(),
                    snapshot.latestDemand(), snapshot.rollingDemand(), snapshot.views(),
                    snapshot.wishlists(), snapshot.livePrice(), snapshot.basePrice(),
                    snapshot.priceChangePercent(), snapshot.trend(), scenario.name(), running.get()
            );
            snapshots.put(current.eventId(), current);
            updated.add(current);
        }
        broadcast("snapshot", updated);
    }

    private void broadcast(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }

    private double expectedDemand(Event event) {
        return Math.max(1.0, event.getCapacity() * event.getSlotPopularity() / 210.0);
    }

    private String trend(double previous, double current) {
        double difference = current - previous;
        if (difference > 0.35) {
            return "RISING";
        }
        if (difference < -0.35) {
            return "FALLING";
        }
        return "STABLE";
    }

    private double percentChange(double value, double base) {
        return round((value / Math.max(1.0, base) - 1.0) * 100.0);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static ThreadFactory namedThread(String name) {
        return task -> {
            Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    @PreDestroy
    public void close() {
        shutdown.set(true);
        producer.shutdownNow();
        consumer.shutdownNow();
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }
}
