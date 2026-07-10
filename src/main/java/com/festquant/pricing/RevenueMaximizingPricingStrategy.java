package com.festquant.pricing;

import com.festquant.domain.PricingEvent;
import com.festquant.domain.PriceRecommendation;
import com.festquant.domain.TicketSalePoint;
import com.festquant.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RevenueMaximizingPricingStrategy implements PricingStrategy {
    private static final double NONLINEAR_WEIGHT = 0.60;       // Main demand signal comes from nonlinear model.
    private static final double TIMESERIES_WEIGHT = 0.40;      // Time-series forecast adds current trend.
    private static final double MAX_INCREASE = 1.20;           // Fairness cap: max 20% rise.
    private static final double MAX_DECREASE = 0.85;           // Avoid excessive revenue loss.
    private static final double PRICE_STEP = 25.0;             // Practical ticket-price step.

    private final JavaNonlinearModelAdapter nonlinearModel;

    public RevenueMaximizingPricingStrategy(JavaNonlinearModelAdapter nonlinearModel) {
        this.nonlinearModel = nonlinearModel;
    }

    @Override
    public PriceRecommendation recommendPrice(
            PricingEvent event,
            List<TicketSalePoint> history,
            ForecastResult forecast,
            List<PriceSimulationRow> simulationRows
    ) {
        TicketSalePoint latest = history.get(history.size() - 1);
        int remainingSeats = Math.max(0, event.getCapacity() - latest.getCumulativeSold());
        double soldRatio = latest.getCumulativeSold() / Math.max(1.0, event.getCapacity());

        double lowerBound = Math.max(event.getMinPrice(), event.getBasePrice() * MAX_DECREASE);
        double upperBound = Math.min(event.getMaxPrice(), event.getBasePrice() * MAX_INCREASE);

        if (soldRatio < 0.30) {
            upperBound = Math.min(upperBound, event.getBasePrice());
        }

        double bestPrice = event.getBasePrice();
        double bestDemand = 0.0;
        double bestRevenue = -1.0;

        for (double candidatePrice = lowerBound; candidatePrice <= upperBound; candidatePrice += PRICE_STEP) {
            DemandFeatures features = buildFeatures(event, latest, candidatePrice);
            double nonlinearDemand = nonlinearModel.predictTickets(event.getCapacity(), features);

            double combinedDemand =
                    NONLINEAR_WEIGHT * nonlinearDemand
                    + TIMESERIES_WEIGHT * forecast.getForecastNext24Hours();

            double expectedTickets = Math.min(remainingSeats, Math.max(0.0, combinedDemand));
            double expectedRevenue = candidatePrice * expectedTickets;

            simulationRows.add(
                    new PriceSimulationRow(
                            event.getEventId(),
                            round2(candidatePrice),
                            round2(expectedTickets),
                            round2(expectedRevenue)
                    )
            );

            if (expectedRevenue > bestRevenue) {
                bestRevenue = expectedRevenue;
                bestPrice = candidatePrice;
                bestDemand = expectedTickets;
            }
        }

        String explanation = buildExplanation(event, latest, forecast, bestPrice, soldRatio);

        return new PriceRecommendation(
                event.getEventId(),
                event.getBasePrice(),
                round2(bestPrice),
                event.getPremiumSeats(),
                round2(bestDemand),
                round2(bestRevenue),
                forecast.getTrendLabel(),
                "PENDING_ADMIN_APPROVAL",
                explanation
        );
    }

    private DemandFeatures buildFeatures(PricingEvent event, TicketSalePoint latest, double candidatePrice) {
        double views = Math.max(0, latest.getViews());
        double logViews = Math.log(views + 1.0);
        double wishlistRate = latest.getWishlists() / Math.max(1.0, views);
        double soldRatio = latest.getCumulativeSold() / Math.max(1.0, event.getCapacity());
        double urgencyIndex = calculateUrgency(event, latest);
        double priceIndex = candidatePrice / Math.max(1.0, event.getBasePrice());

        return new DemandFeatures(
                logViews,
                wishlistRate,
                soldRatio,
                urgencyIndex,
                event.getSlotPopularity(),
                priceIndex
        );
    }

    private double calculateUrgency(PricingEvent event, TicketSalePoint latest) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime saleTime = latest.getTimestamp();
            LocalDateTime eventTime = LocalDateTime.parse(event.getEventDatetime(), formatter);

            long hoursLeft = java.time.Duration.between(saleTime, eventTime).toHours();
            double daysLeft = Math.max(1.0, hoursLeft / 24.0);

            return Math.min(1.0, 7.0 / daysLeft);
        } catch (Exception ex) {
            return 0.50;
        }
    }

    private String buildExplanation(
            PricingEvent event,
            TicketSalePoint latest,
            ForecastResult forecast,
            double bestPrice,
            double soldRatio
    ) {
        String direction;

        if (bestPrice > event.getBasePrice()) {
            direction = "increased";
        } else if (bestPrice < event.getBasePrice()) {
            direction = "reduced";
        } else {
            direction = "kept unchanged";
        }

        return "Price was " + direction + " from Rs " + String.format("%.0f", event.getBasePrice()) +
                " to Rs " + String.format("%.0f", bestPrice) +
                " because trend is " + forecast.getTrendLabel() +
                ", sold ratio is " + String.format("%.1f", soldRatio * 100.0) + "%, " +
                "latest views are " + latest.getViews() +
                ", and nonlinear demand is combined with Holt time-series forecasting. " +
                "The final price respects min/max limits and the 20% fairness cap.";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
