package com.festquant.pricing;

public class PriceSimulationRow {
    private final String eventId;              // Event being simulated.
    private final double candidatePrice;       // Price tested by optimizer.
    private final double predictedDemand;      // Expected tickets sold at this price.
    private final double expectedRevenue;      // candidatePrice * predictedDemand.

    public PriceSimulationRow(
            String eventId,
            double candidatePrice,
            double predictedDemand,
            double expectedRevenue
    ) {
        this.eventId = eventId;
        this.candidatePrice = candidatePrice;
        this.predictedDemand = predictedDemand;
        this.expectedRevenue = expectedRevenue;
    }

    public String getEventId() { return eventId; }
    public double getCandidatePrice() { return candidatePrice; }
    public double getPredictedDemand() { return predictedDemand; }
    public double getExpectedRevenue() { return expectedRevenue; }
}
