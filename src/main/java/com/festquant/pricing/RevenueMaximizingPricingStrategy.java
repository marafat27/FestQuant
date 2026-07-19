/**
 * Contains the revenue maximizing pricing strategy implementation used by FestQuant.
 */
package com.festquant.pricing;

import com.festquant.domain.PricingEvent;
import com.festquant.domain.PriceRecommendation;
import com.festquant.domain.TicketSalePoint;
import com.festquant.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tests practical candidate prices and selects the one with the highest expected
 * revenue while respecting event limits and the fairness band.
 */
public class RevenueMaximizingPricingStrategy implements PricingStrategy {
    // Stores the nonlinear weight used by this class.
    private static final double NONLINEAR_WEIGHT = 0.60;       // Main demand signal comes from nonlinear model.
    // Stores the timeseries weight used by this class.
    private static final double TIMESERIES_WEIGHT = 0.40;      // Time-series forecast adds current trend.
    // Stores the max increase used by this class.
    private static final double MAX_INCREASE = 1.20;           // Fairness cap: max 20% rise.
    // Stores the max decrease used by this class.
    private static final double MAX_DECREASE = 0.85;           // Avoid excessive revenue loss.
    // Stores the price step used by this class.
    private static final double PRICE_STEP = 25.0;             // Practical ticket-price step.

    // Stores the nonlinear model used by this class.
    private final JavaNonlinearModelAdapter nonlinearModel;

    /**
     * Creates a RevenueMaximizingPricingStrategy with the values needed by this component.
     */
    public RevenueMaximizingPricingStrategy(JavaNonlinearModelAdapter nonlinearModel) {
        this.nonlinearModel = nonlinearModel;
    }

    /**
     * Simulates allowed ticket prices and returns the strongest revenue option.
     */
    @Override
    public PriceRecommendation recommendPrice(
            PricingEvent event,
            List<TicketSalePoint> history,
            ForecastResult forecast,
            List<PriceSimulationRow> simulationRows
    ) {
        // Holds the latest for this calculation.
        TicketSalePoint latest = history.get(history.size() - 1);
        // Remaining capacity is the largest number of tickets that can still be sold.
        int remainingSeats = Math.max(0, event.getCapacity() - latest.getCumulativeSold());
        // Sold ratio = cumulative tickets sold / event capacity.
        double soldRatio = latest.getCumulativeSold() / Math.max(1.0, event.getCapacity());

        // Lower bound is the stricter of the event minimum and 85% of base price.
        double lowerBound = Math.max(event.getMinPrice(), event.getBasePrice() * MAX_DECREASE);
        // Upper bound is the stricter of the event maximum and 120% of base price.
        double upperBound = Math.min(event.getMaxPrice(), event.getBasePrice() * MAX_INCREASE);

        if (soldRatio < 0.30) {
            upperBound = Math.min(upperBound, event.getBasePrice());
        }

        // Holds the best price for this calculation.
        double bestPrice = event.getBasePrice();
        // Holds the best demand for this calculation.
        double bestDemand = 0.0;
        // Holds the best revenue for this calculation.
        double bestRevenue = -1.0;

        // Uses candidate price for the current item in the loop.
        for (double candidatePrice = lowerBound; candidatePrice <= upperBound; candidatePrice += PRICE_STEP) {
            // Holds the features for this calculation.
            DemandFeatures features = buildFeatures(event, latest, candidatePrice);
            // Regression demand is capacity multiplied by the logistic demand ratio.
            double nonlinearDemand = nonlinearModel.predictTickets(event.getCapacity(), features);

            // Blended demand = 0.60*regression demand + 0.40*Holt forecast.
            double combinedDemand =
                    NONLINEAR_WEIGHT * nonlinearDemand
                    + TIMESERIES_WEIGHT * forecast.getForecastNext24Hours();

            // Expected tickets are non-negative and cannot exceed remaining capacity.
            double expectedTickets = Math.min(remainingSeats, Math.max(0.0, combinedDemand));
            // Revenue equation: R(p) = candidate price p * expected tickets Q(p).
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

        // Holds the explanation for this calculation.
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

    /**
     * Builds normalized inputs for the nonlinear demand model.
     */
    private DemandFeatures buildFeatures(PricingEvent event, TicketSalePoint latest, double candidatePrice) {
        // Holds the views for this calculation.
        double views = Math.max(0, latest.getViews());
        // Log scaling reduces the effect of very large view counts: logViews = ln(views + 1).
        double logViews = Math.log(views + 1.0);
        // Wishlist rate = wishlists / views and represents stronger purchase intent.
        double wishlistRate = latest.getWishlists() / Math.max(1.0, views);
        // Sold ratio = cumulative sold / capacity.
        double soldRatio = latest.getCumulativeSold() / Math.max(1.0, event.getCapacity());
        // Holds the urgency index for this calculation.
        double urgencyIndex = calculateUrgency(event, latest);
        // Price index = candidate price / base price; values above 1 mean a price increase.
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

    /**
     * Returns an urgency score that approaches one during the final week.
     */
    private double calculateUrgency(PricingEvent event, TicketSalePoint latest) {
        try {
            // Holds the formatter for this calculation.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            // Holds the sale time for this calculation.
            LocalDateTime saleTime = latest.getTimestamp();
            // Holds the event time for this calculation.
            LocalDateTime eventTime = LocalDateTime.parse(event.getEventDatetime(), formatter);

            // Holds the hours left for this calculation.
            long hoursLeft = java.time.Duration.between(saleTime, eventTime).toHours();
            // Holds the days left for this calculation.
            double daysLeft = Math.max(1.0, hoursLeft / 24.0);

            // Urgency = min(1, 7/days left), so the final seven days have the strongest signal.
            return Math.min(1.0, 7.0 / daysLeft);
        } catch (Exception ex) {
            return 0.50;
        }
    }

    /**
     * Builds the short business explanation shown beside a recommendation.
     */
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

    /**
     * Rounds currency and forecast values to two decimal places.
     */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
