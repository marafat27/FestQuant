/**
 * Contains the forecast result implementation used by FestQuant.
 */
package com.festquant.model;

/**
 * Represents the forecast result part of the FestQuant application.
 */
public class ForecastResult {
    // Stores the event id used by this class.
    private final String eventId;              // Forecasted event.
    // Stores the forecast next24 hours used by this class.
    private final double forecastNext24Hours;  // Holt forecast for next 24 hours.
    // Stores the trend label used by this class.
    private final String trendLabel;           // RISING, STABLE, or FALLING.
    // Stores the confidence used by this class.
    private final double confidence;           // Simple confidence score.
    // Stores the explanation used by this class.
    private final String explanation;          // Forecast explanation.

    /**
     * Creates a ForecastResult with the values needed by this component.
     */
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

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns forecast next24 hours.
     */
    public double getForecastNext24Hours() { return forecastNext24Hours; }
    /**
     * Returns trend label.
     */
    public String getTrendLabel() { return trendLabel; }
    /**
     * Returns confidence.
     */
    public double getConfidence() { return confidence; }
    /**
     * Returns explanation.
     */
    public String getExplanation() { return explanation; }
}
