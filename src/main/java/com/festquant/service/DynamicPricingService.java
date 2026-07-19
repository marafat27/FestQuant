/**
 * Contains the dynamic pricing service implementation used by FestQuant.
 */
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

/**
 * Coordinates the business logic for dynamic pricing.
 */
public class DynamicPricingService {
    // Stores the event repository used by this class.
    private final CsvPricingEventRepository eventRepository;
    // Stores the sales repository used by this class.
    private final CsvSalesRepository salesRepository;
    // Stores the forecaster used by this class.
    private final TimeSeriesForecaster forecaster;
    // Stores the pricing strategy used by this class.
    private final PricingStrategy pricingStrategy;

    /**
     * Creates a DynamicPricingService with the values needed by this component.
     */
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

    /**
     * Recommends all prices.
     */
    public PricingRunResult recommendAllPrices() {
        // Holds the events for this calculation.
        List<PricingEvent> events = eventRepository.findAll();
        Map<String, List<TicketSalePoint>> salesByEvent = salesRepository.groupByEvent();

        // Holds the recommendations for this calculation.
        List<PriceRecommendation> recommendations = Collections.synchronizedList(new ArrayList<>());
        // Holds the simulations for this calculation.
        List<PriceSimulationRow> simulations = Collections.synchronizedList(new ArrayList<>());

        // Holds the executor service for this calculation.
        ExecutorService executorService = Executors.newFixedThreadPool(
                Math.min(4, Math.max(1, events.size()))
        );

        // Holds the futures for this calculation.
        List<Future<?>> futures = new ArrayList<>();

        // Uses event for the current item in the loop.
        for (PricingEvent event : events) {
            futures.add(executorService.submit(() -> {
                // Holds the history for this calculation.
                List<TicketSalePoint> history = salesByEvent.getOrDefault(event.getEventId(), new ArrayList<>());

                if (history.isEmpty()) {
                    return;
                }

                // Holds the forecast for this calculation.
                ForecastResult forecast = forecaster.forecast(event.getEventId(), history);
                // Holds the recommendation for this calculation.
                PriceRecommendation recommendation =
                        pricingStrategy.recommendPrice(event, history, forecast, simulations);

                recommendations.add(recommendation);
            }));
        }

        // Uses future for the current item in the loop.
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

    /**
     * Represents the pricing run result part of the FestQuant application.
     */
    public static class PricingRunResult {
        // Stores the recommendations used by this class.
        private final List<PriceRecommendation> recommendations;
        // Stores the simulations used by this class.
        private final List<PriceSimulationRow> simulations;

        /**
         * Handles the pricing run result step.
         */
        public PricingRunResult(
                List<PriceRecommendation> recommendations,
                List<PriceSimulationRow> simulations
        ) {
            this.recommendations = recommendations;
            this.simulations = simulations;
        }

        /**
         * Returns recommendations.
         */
        public List<PriceRecommendation> getRecommendations() { return recommendations; }
        /**
         * Returns simulations.
         */
        public List<PriceSimulationRow> getSimulations() { return simulations; }
    }
}
