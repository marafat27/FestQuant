package com.festquant.domain;

/**
 * Immutable output of the pricing engine. The richer representation is shared
 * by the REST API, H2 repository, JSON exporter and both dashboards.
 */
public final class PriceRecommendation {
    private final String eventId;
    private final double basePrice;
    private final double recommendedPrice;
    private final int premiumSeats;
    private final double forecastDemand;
    private final double expectedRevenue;
    private final String trendLabel;
    private final String status;
    private final String explanation;

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

    public String getEventId() { return eventId; }
    public double getBasePrice() { return basePrice; }
    public double getRecommendedPrice() { return recommendedPrice; }
    public int getPremiumSeats() { return premiumSeats; }
    public double getForecastDemand() { return forecastDemand; }
    public double getExpectedRevenue() { return expectedRevenue; }
    public String getTrendLabel() { return trendLabel; }
    public String getStatus() { return status; }
    public String getExplanation() { return explanation; }

    /** Kept for compatibility with the original dashboard contract. */
    public String getReason() { return explanation; }
}
