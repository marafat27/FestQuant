package com.festquant.stream;

import com.festquant.domain.Event;

/**
 * Converts a rolling demand index into a stable, bounded live ticket price.
 * Historical model output provides the anchor; current demand controls the
 * movement around it.
 */
public final class LivePricingPolicy {
    private static final double MIN_FAIRNESS_FACTOR = 0.85;
    private static final double MAX_FAIRNESS_FACTOR = 1.20;
    private static final double PRICE_STEP = 10.0;

    public double recommend(Event event, double historicalModelPrice, double demandIndex) {
        double boundedIndex = clamp(demandIndex, 0.25, 1.90);
        double anchor = historicalModelPrice * 0.60 + event.getBasePrice() * 0.40;
        double demandFactor = 0.87 + 0.16 * boundedIndex;
        double candidate = anchor * demandFactor;

        double lowerBound = Math.max(event.getMinPrice(), event.getBasePrice() * MIN_FAIRNESS_FACTOR);
        double upperBound = Math.min(event.getMaxPrice(), event.getBasePrice() * MAX_FAIRNESS_FACTOR);
        return roundToStep(clamp(candidate, lowerBound, upperBound));
    }

    private double roundToStep(double value) {
        return Math.round(value / PRICE_STEP) * PRICE_STEP;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
