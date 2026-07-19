/**
 * Contains the live pricing policy implementation used by FestQuant.
 */
package com.festquant.stream;

import com.festquant.domain.Event;

/**
 * Converts a rolling demand index into a stable, bounded live ticket price.
 * Historical model output provides the anchor; current demand controls the
 * movement around it.
 */
public final class LivePricingPolicy {
    // Stores the min fairness factor used by this class.
    private static final double MIN_FAIRNESS_FACTOR = 0.85;
    // Stores the max fairness factor used by this class.
    private static final double MAX_FAIRNESS_FACTOR = 1.20;
    // Stores the price step used by this class.
    private static final double PRICE_STEP = 10.0;

    /**
     * Adjusts the historical model price with the current rolling demand index.
     */
    public double recommend(Event event, double historicalModelPrice, double demandIndex) {
        // Demand index = rolling demand / expected demand, limited to avoid sudden spikes.
        double boundedIndex = clamp(demandIndex, 0.25, 1.90);
        // Anchor = 60% historical recommendation + 40% event base price.
        double anchor = historicalModelPrice * 0.60 + event.getBasePrice() * 0.40;
        // Demand multiplier = 0.87 + 0.16*demand index.
        double demandFactor = 0.87 + 0.16 * boundedIndex;
        // Candidate live price = anchor * demand multiplier.
        double candidate = anchor * demandFactor;

        // The live price cannot fall below the event minimum or 85% of base price.
        double lowerBound = Math.max(event.getMinPrice(), event.getBasePrice() * MIN_FAIRNESS_FACTOR);
        // The live price cannot exceed the event maximum or 120% of base price.
        double upperBound = Math.min(event.getMaxPrice(), event.getBasePrice() * MAX_FAIRNESS_FACTOR);
        return roundToStep(clamp(candidate, lowerBound, upperBound));
    }

    /**
     * Rounds the live price to a practical ten-rupee step.
     */
    private double roundToStep(double value) {
        return Math.round(value / PRICE_STEP) * PRICE_STEP;
    }

    /**
     * Keeps a numeric value inside its allowed lower and upper limits.
     */
    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
