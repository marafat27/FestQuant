package com.festquant.model;

import com.festquant.domain.TicketSalePoint;

import java.util.List;

public class TimeSeriesForecaster {
    private static final double ALPHA = 0.60;       // Recent demand gets higher weight.
    private static final double BETA = 0.30;        // Trend is updated moderately.
    private static final int HORIZON = 24;          // Forecast next 24 time buckets.

    public ForecastResult forecast(String eventId, List<TicketSalePoint> history) {
        if (history == null || history.isEmpty()) {
            return new ForecastResult(eventId, 0.0, "UNKNOWN", 0.0, "No sales history available.");
        }

        if (history.size() == 1) {
            double demand = history.get(0).getTicketsSoldInterval();

            return new ForecastResult(
                    eventId,
                    demand,
                    "STABLE",
                    0.50,
                    "Only one sales point was available, so latest demand was used."
            );
        }

        double level = history.get(0).getTicketsSoldInterval();
        double trend = history.get(1).getTicketsSoldInterval() - level;

        for (int i = 1; i < history.size(); i++) {
            double actual = history.get(i).getTicketsSoldInterval();
            double previousLevel = level;

            level = ALPHA * actual + (1.0 - ALPHA) * (level + trend);
            trend = BETA * (level - previousLevel) + (1.0 - BETA) * trend;
        }

        double forecast = Math.max(0.0, level + HORIZON * trend);
        String trendLabel = labelTrend(trend);
        double confidence = calculateConfidence(history);

        String explanation =
                "Holt trend smoothing was used with alpha=0.60 and beta=0.30. " +
                "The latest estimated trend is " + trendLabel + ".";

        return new ForecastResult(eventId, forecast, trendLabel, confidence, explanation);
    }

    private String labelTrend(double trend) {
        if (trend > 0.75) {
            return "RISING";
        }

        if (trend < -0.75) {
            return "FALLING";
        }

        return "STABLE";
    }

    private double calculateConfidence(List<TicketSalePoint> history) {
        double total = 0.0;

        for (TicketSalePoint point : history) {
            total += point.getTicketsSoldInterval();
        }

        double mean = total / history.size();
        double squaredError = 0.0;

        for (TicketSalePoint point : history) {
            double diff = point.getTicketsSoldInterval() - mean;
            squaredError += diff * diff;
        }

        double stdDev = Math.sqrt(squaredError / history.size());
        double volatilityRatio = stdDev / Math.max(1.0, mean);

        return Math.max(0.40, Math.min(0.95, 1.0 - volatilityRatio));
    }
}
