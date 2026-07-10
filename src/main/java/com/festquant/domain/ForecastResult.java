package com.festquant.domain;

public class ForecastResult {
    private final String eventId;
    private final double forecastNext24Hours;
    private final String trend;
    private final double confidence;
    private final String explanation;

    public ForecastResult(String eventId, double forecastNext24Hours, String trend, double confidence, String explanation) {
        this.eventId = eventId;
        this.forecastNext24Hours = forecastNext24Hours;
        this.trend = trend;
        this.confidence = confidence;
        this.explanation = explanation;
    }

    public String getEventId() { return eventId; }
    public double getForecastNext24Hours() { return forecastNext24Hours; }
    public String getTrend() { return trend; }
    public double getConfidence() { return confidence; }
    public String getExplanation() { return explanation; }
}
