/**
 * Contains the price recommendation implementation used by FestQuant.
 */
package com.festquant.domain;

/**
 * Immutable output of the pricing engine. The richer representation is shared
 * by the REST API, H2 repository, JSON exporter and both dashboards.
 */
public final class PriceRecommendation {
    // Stores the event id used by this class.
    private final String eventId;
    // Stores the base price used by this class.
    private final double basePrice;
    // Stores the recommended price used by this class.
    private final double recommendedPrice;
    // Stores the premium seats used by this class.
    private final int premiumSeats;
    // Stores the forecast demand used by this class.
    private final double forecastDemand;
    // Stores the expected revenue used by this class.
    private final double expectedRevenue;
    // Stores the trend label used by this class.
    private final String trendLabel;
    // Stores the status used by this class.
    private final String status;
    // Stores the explanation used by this class.
    private final String explanation;

    /**
     * Creates a PriceRecommendation with the values needed by this component.
     */
    public PriceRecommendation(String eventId, double basePrice, double recommendedPrice,
                               int premiumSeats, double forecastDemand, double expectedRevenue,
                               String trendLabel, String status, String explanation) {
        this.eventId = eventId;
        this.basePrice = basePrice;
        this.recommendedPrice = recommendedPrice;
        this.premiumSeats = premiumSeats;
        this.forecastDemand = forecastDemand;
        this.expectedRevenue = expectedRevenue;
        this.trendLabel = trendLabel;
        this.status = status;
        this.explanation = explanation;
    }

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns base price.
     */
    public double getBasePrice() { return basePrice; }
    /**
     * Returns recommended price.
     */
    public double getRecommendedPrice() { return recommendedPrice; }
    /**
     * Returns premium seats.
     */
    public int getPremiumSeats() { return premiumSeats; }
    /**
     * Returns forecast demand.
     */
    public double getForecastDemand() { return forecastDemand; }
    /**
     * Returns expected revenue.
     */
    public double getExpectedRevenue() { return expectedRevenue; }
    /**
     * Returns trend label.
     */
    public String getTrendLabel() { return trendLabel; }
    /**
     * Returns status.
     */
    public String getStatus() { return status; }
    /**
     * Returns explanation.
     */
    public String getExplanation() { return explanation; }

    /** Kept for compatibility with the original dashboard contract. */
    public String getReason() { return explanation; }
}
