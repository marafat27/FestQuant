/**
 * Contains the price simulation row implementation used by FestQuant.
 */
package com.festquant.pricing;

/**
 * Represents the price simulation row part of the FestQuant application.
 */
public class PriceSimulationRow {
    // Stores the event id used by this class.
    private final String eventId;              // Event being simulated.
    // Stores the candidate price used by this class.
    private final double candidatePrice;       // Price tested by optimizer.
    // Stores the predicted demand used by this class.
    private final double predictedDemand;      // Expected tickets sold at this price.
    // Stores the expected revenue used by this class.
    private final double expectedRevenue;      // candidatePrice * predictedDemand.

    /**
     * Creates a PriceSimulationRow with the values needed by this component.
     */
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

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns candidate price.
     */
    public double getCandidatePrice() { return candidatePrice; }
    /**
     * Returns predicted demand.
     */
    public double getPredictedDemand() { return predictedDemand; }
    /**
     * Returns expected revenue.
     */
    public double getExpectedRevenue() { return expectedRevenue; }
}
