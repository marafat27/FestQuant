package com.festquant.model;

public class ForecastResult {
    private final String eventId;              // Forecasted event.
    private final double forecastNext24Hours;  // Holt forecast for next 24 hours.
    private final String trendLabel;           // RISING, STABLE, or FALLING.
    private final double confidence;           // Simple confidence score.
    private final String explanation;          // Forecast explanation.

    public ForecastResult(
            String eventId,
            double forecastNext24Hours,
            String trendLabel,
            double confidence,
            String explanation
    ) {
        this.eventId = eventId;
        this.forecastNext24Hours = forecastNext24Hours;
        this.trendLabel = trendLabel;
        this.confidence = confidence;
        this.explanation = explanation;
    }

    public String getEventId() { return eventId; }
    public double getForecastNext24Hours() { return forecastNext24Hours; }
    public String getTrendLabel() { return trendLabel; }
    public double getConfidence() { return confidence; }
    public String getExplanation() { return explanation; }
}
