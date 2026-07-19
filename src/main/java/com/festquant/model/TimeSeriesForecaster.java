/**
 * Contains the time series forecaster implementation used by FestQuant.
 */
package com.festquant.model;

import com.festquant.domain.TicketSalePoint;

import java.util.List;

/**
 * Forecasts near-term ticket demand with Holt's level-and-trend smoothing.
 * This model reacts to recent sales without discarding the longer trend.
 */
public class TimeSeriesForecaster {
    // Stores the alpha used by this class.
    private static final double ALPHA = 0.60;       // Recent demand gets higher weight.
    // Stores the beta used by this class.
    private static final double BETA = 0.30;        // Trend is updated moderately.
    // Stores the horizon used by this class.
    private static final int HORIZON = 24;          // Forecast next 24 time buckets.

    /**
     * Estimates the next 24 sales intervals and labels the recent direction.
     */
    public ForecastResult forecast(String eventId, List<TicketSalePoint> history) {
        if (history == null || history.isEmpty()) {
            return new ForecastResult(eventId, 0.0, "UNKNOWN", 0.0, "No sales history available.");
        }

        if (history.size() == 1) {
            // Holds the demand for this calculation.
            double demand = history.get(0).getTicketsSoldInterval();

            return new ForecastResult(
                    eventId,
                    demand,
                    "STABLE",
                    0.50,
                    "Only one sales point was available, so latest demand was used."
            );
        }

        // Initial level L0 is the first observed ticket count.
        double level = history.get(0).getTicketsSoldInterval();
        // Initial trend T0 is the change between the first two observations.
        double trend = history.get(1).getTicketsSoldInterval() - level;

        // Uses i for the current item in the loop.
        for (int i = 1; i < history.size(); i++) {
            // Holds the actual for this calculation.
            double actual = history.get(i).getTicketsSoldInterval();
            // Holds the previous level for this calculation.
            double previousLevel = level;

            // Holt level: Lt = alpha*Yt + (1-alpha)*(L(t-1) + T(t-1)).
            level = ALPHA * actual + (1.0 - ALPHA) * (level + trend);
            // Holt trend: Tt = beta*(Lt - L(t-1)) + (1-beta)*T(t-1).
            trend = BETA * (level - previousLevel) + (1.0 - BETA) * trend;
        }

        // h-step forecast: Y(t+h) = Lt + h*Tt, bounded at zero tickets.
        double forecast = Math.max(0.0, level + HORIZON * trend);
        // Holds the trend label for this calculation.
        String trendLabel = labelTrend(trend);
        // Holds the confidence for this calculation.
        double confidence = calculateConfidence(history);

        // Holds the explanation for this calculation.
        String explanation =
                "Holt trend smoothing was used with alpha=0.60 and beta=0.30. " +
                "The latest estimated trend is " + trendLabel + ".";

        return new ForecastResult(eventId, forecast, trendLabel, confidence, explanation);
    }

    /**
     * Converts the numeric trend into a simple rising, stable or falling label.
     */
    private String labelTrend(double trend) {
        if (trend > 0.75) {
            return "RISING";
        }

        if (trend < -0.75) {
            return "FALLING";
        }

        return "STABLE";
    }

    /**
     * Derives a bounded confidence score from relative sales volatility.
     */
    private double calculateConfidence(List<TicketSalePoint> history) {
        // Holds the total for this calculation.
        double total = 0.0;

        // Uses point for the current item in the loop.
        for (TicketSalePoint point : history) {
            total += point.getTicketsSoldInterval();
        }

        // Holds the mean for this calculation.
        double mean = total / history.size();
        // Holds the squared error for this calculation.
        double squaredError = 0.0;

        // Uses point for the current item in the loop.
        for (TicketSalePoint point : history) {
            // Holds the diff for this calculation.
            double diff = point.getTicketsSoldInterval() - mean;
            squaredError += diff * diff;
        }

        // Holds the std dev for this calculation.
        double stdDev = Math.sqrt(squaredError / history.size());
        // Holds the volatility ratio for this calculation.
        double volatilityRatio = stdDev / Math.max(1.0, mean);

        // Confidence = 1 - standardDeviation/mean, limited to the range [0.40, 0.95].
        return Math.max(0.40, Math.min(0.95, 1.0 - volatilityRatio));
    }
}
