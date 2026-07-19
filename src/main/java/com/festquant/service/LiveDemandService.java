/**
 * Contains the live demand service implementation used by FestQuant.
 */
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
    // Stores the window size used by this class.
    private static final int WINDOW_SIZE = 8;
    // Stores the interval seconds used by this class.
    private static final int INTERVAL_SECONDS = 2;

    // Stores the analytics used by this class.
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
    // Stores the producer used by this class.
    private final ScheduledExecutorService producer =
            Executors.newSingleThreadScheduledExecutor(namedThread("festquant-live-producer"));
    // Stores the consumer used by this class.
    private final ExecutorService consumer =
            Executors.newSingleThreadExecutor(namedThread("festquant-live-consumer"));
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicLong sequence = new AtomicLong();
    private final Random random = new Random(42L);
    private volatile DemandScenario scenario = DemandScenario.NORMAL;

    /**
     * Creates a LiveDemandService with the values needed by this component.
     */
    public LiveDemandService(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    /**
     * Handles the initialise step.
     */
    @PostConstruct
    public void initialise() {
        analytics.events().forEach(event -> eventsById.put(event.getEventId(), event));
        analytics.recommendations().forEach(recommendation ->
                historicalPrices.put(recommendation.getEventId(), recommendation.getRecommendedPrice()));

        // Holds the now for this calculation.
        Instant now = Instant.now();
        // Uses event for the current item in the loop.
        for (Event event : eventsById.values()) {
            // Holds the starting price for this calculation.
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

    /**
     * Handles the snapshots step.
     */
    public List<LiveDemandSnapshot> snapshots() {
        return snapshots.values().stream()
                .sorted(Comparator.comparing(LiveDemandSnapshot::eventName))
                .toList();
    }

    /**
     * Handles the subscribe step.
     */
    public SseEmitter subscribe() {
        // Holds the emitter for this calculation.
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

    /**
     * Produces one simulated market pulse for every event.
     */
    private void produceBatch() {
        if (!running.get() || shutdown.get()) {
            return;
        }

        // Holds the tick for this calculation.
        long tick = sequence.incrementAndGet();
        // Holds the observed at for this calculation.
        Instant observedAt = Instant.now();
        // Uses event for the current item in the loop.
        for (Event event : eventsById.values()) {
            // Baseline ticket demand expected for this event and time slot.
            double expected = expectedDemand(event);
            // A smooth wave prevents every simulated pulse from looking identical.
            double wave = 0.88 + 0.20 * Math.sin((tick + Math.abs(event.getEventId().hashCode() % 7)) / 2.6);
            // Small seeded noise gives variation while keeping demonstrations repeatable.
            double noise = 0.76 + random.nextDouble() * 0.48;
            // Holds the tickets for this calculation.
            int tickets = Math.max(0, (int) Math.round(expected * scenario.multiplier() * wave * noise));
            // Holds the views for this calculation.
            int views = Math.max(15, tickets * (18 + random.nextInt(15)) + random.nextInt(90));
            // Holds the wishlists for this calculation.
            int wishlists = Math.max(1, (int) Math.round(views * (0.08 + random.nextDouble() * 0.12)));
            queue.offer(new LiveTicketSale(
                    event.getEventId(), observedAt, tick, views, wishlists, tickets
            ));
        }
    }

    /**
     * Consumes queued ticket-sale messages until the service shuts down.
     */
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

    /**
     * Updates one event's rolling demand, live price and browser snapshot.
     */
    private void process(LiveTicketSale sale) {
        // Holds the event for this calculation.
        Event event = eventsById.get(sale.eventId());
        if (event == null) {
            return;
        }

        // Holds the window for this calculation.
        Deque<Integer> window = demandWindows.get(sale.eventId());
        window.addLast(sale.ticketsSold());
        while (window.size() > WINDOW_SIZE) {
            window.removeFirst();
        }

        // Rolling demand is the mean ticket count across the latest eight pulses.
        double rollingDemand = window.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        // Holds the previous demand for this calculation.
        double previousDemand = previousRollingDemand.getOrDefault(sale.eventId(), rollingDemand);
        previousRollingDemand.put(sale.eventId(), rollingDemand);
        // Demand index = rolling demand / expected baseline demand.
        double demandIndex = rollingDemand / expectedDemand(event);

        // Holds the historical price for this calculation.
        double historicalPrice = historicalPrices.getOrDefault(sale.eventId(), event.getBasePrice());
        // Holds the previous price for this calculation.
        double previousPrice = currentPrices.getOrDefault(sale.eventId(), historicalPrice);
        // Policy target applies current demand while respecting the fairness band.
        double targetPrice = pricingPolicy.recommend(event, historicalPrice, demandIndex);
        // Smoothed live price = 55% previous price + 45% new target, rounded to Rs 10.
        double livePrice = Math.round((previousPrice * 0.55 + targetPrice * 0.45) / 10.0) * 10.0;
        currentPrices.put(sale.eventId(), livePrice);

        // Holds the snapshot for this calculation.
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

    /**
     * Handles the publish control state step.
     */
    private void publishControlState() {
        // Holds the updated for this calculation.
        List<LiveDemandSnapshot> updated = new ArrayList<>();
        // Uses snapshot for the current item in the loop.
        for (LiveDemandSnapshot snapshot : snapshots()) {
            // Holds the current for this calculation.
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

    /**
     * Handles the broadcast step.
     */
    private void broadcast(String eventName, Object data) {
        // Uses emitter for the current item in the loop.
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }

    /**
     * Estimates a simple baseline from capacity and slot popularity.
     */
    private double expectedDemand(Event event) {
        // Baseline = capacity * slot popularity / 210, with a minimum of one ticket.
        return Math.max(1.0, event.getCapacity() * event.getSlotPopularity() / 210.0);
    }

    /**
     * Handles the trend step.
     */
    private String trend(double previous, double current) {
        // Holds the difference for this calculation.
        double difference = current - previous;
        if (difference > 0.35) {
            return "RISING";
        }
        if (difference < -0.35) {
            return "FALLING";
        }
        return "STABLE";
    }

    /**
     * Calculates percentage movement from the base price.
     */
    private double percentChange(double value, double base) {
        // Percentage change = ((current/base) - 1) * 100.
        return round((value / Math.max(1.0, base) - 1.0) * 100.0);
    }

    /**
     * Handles the round step.
     */
    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Handles the named thread step.
     */
    private static ThreadFactory namedThread(String name) {
        return task -> {
            // Holds the thread for this calculation.
            Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Handles the close step.
     */
    @PreDestroy
    public void close() {
        shutdown.set(true);
        producer.shutdownNow();
        consumer.shutdownNow();
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }
}
