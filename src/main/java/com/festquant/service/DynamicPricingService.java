package com.festquant.service;

import com.festquant.domain.PricingEvent;
import com.festquant.domain.PriceRecommendation;
import com.festquant.domain.TicketSalePoint;
import com.festquant.model.ForecastResult;
import com.festquant.model.TimeSeriesForecaster;
import com.festquant.pricing.PriceSimulationRow;
import com.festquant.pricing.PricingStrategy;
import com.festquant.repository.CsvPricingEventRepository;
import com.festquant.repository.CsvSalesRepository;

import java.util.*;
import java.util.concurrent.*;

public class DynamicPricingService {
    private final CsvPricingEventRepository eventRepository;
    private final CsvSalesRepository salesRepository;
    private final TimeSeriesForecaster forecaster;
    private final PricingStrategy pricingStrategy;

    public DynamicPricingService(
            CsvPricingEventRepository eventRepository,
            CsvSalesRepository salesRepository,
            TimeSeriesForecaster forecaster,
            PricingStrategy pricingStrategy
    ) {
        this.eventRepository = eventRepository;
        this.salesRepository = salesRepository;
        this.forecaster = forecaster;
        this.pricingStrategy = pricingStrategy;
    }

    public PricingRunResult recommendAllPrices() {
        List<PricingEvent> events = eventRepository.findAll();
        Map<String, List<TicketSalePoint>> salesByEvent = salesRepository.groupByEvent();

        List<PriceRecommendation> recommendations = Collections.synchronizedList(new ArrayList<>());
        List<PriceSimulationRow> simulations = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executorService = Executors.newFixedThreadPool(
                Math.min(4, Math.max(1, events.size()))
        );

        List<Future<?>> futures = new ArrayList<>();

        for (PricingEvent event : events) {
            futures.add(executorService.submit(() -> {
                List<TicketSalePoint> history = salesByEvent.getOrDefault(event.getEventId(), new ArrayList<>());

                if (history.isEmpty()) {
                    return;
                }

                ForecastResult forecast = forecaster.forecast(event.getEventId(), history);
                PriceRecommendation recommendation =
                        pricingStrategy.recommendPrice(event, history, forecast, simulations);

                recommendations.add(recommendation);
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Pricing execution was interrupted.", ex);
            } catch (ExecutionException ex) {
                throw new RuntimeException("Pricing execution failed.", ex);
            }
        }

        executorService.shutdown();

        recommendations.sort(Comparator.comparing(PriceRecommendation::getEventId));
        simulations.sort(Comparator
                .comparing(PriceSimulationRow::getEventId)
                .thenComparingDouble(PriceSimulationRow::getCandidatePrice));

        return new PricingRunResult(recommendations, simulations);
    }

    public static class PricingRunResult {
        private final List<PriceRecommendation> recommendations;
        private final List<PriceSimulationRow> simulations;

        public PricingRunResult(
                List<PriceRecommendation> recommendations,
                List<PriceSimulationRow> simulations
        ) {
            this.recommendations = recommendations;
            this.simulations = simulations;
        }

        public List<PriceRecommendation> getRecommendations() { return recommendations; }
        public List<PriceSimulationRow> getSimulations() { return simulations; }
    }
}
