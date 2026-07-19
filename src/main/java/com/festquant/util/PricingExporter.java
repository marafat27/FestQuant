/**
 * Contains the pricing exporter implementation used by FestQuant.
 */
package com.festquant.util;

import com.festquant.domain.PriceRecommendation;
import com.festquant.pricing.PriceSimulationRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Represents the pricing exporter part of the FestQuant application.
 */
public class PricingExporter {
    /**
     * Exports recommendations json.
     */
    public void exportRecommendationsJson(List<PriceRecommendation> recommendations, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("[\n");

                // Uses i for the current item in the loop.
                for (int i = 0; i < recommendations.size(); i++) {
                    // Holds the rec for this calculation.
                    PriceRecommendation rec = recommendations.get(i);

                    writer.write("  {\n");
                    writer.write("    \"eventId\": \"" + rec.getEventId() + "\",\n");
                    writer.write("    \"basePrice\": " + rec.getBasePrice() + ",\n");
                    writer.write("    \"recommendedPrice\": " + rec.getRecommendedPrice() + ",\n");
                    writer.write("    \"premiumSeats\": " + rec.getPremiumSeats() + ",\n");
                    writer.write("    \"forecastDemand\": " + rec.getForecastDemand() + ",\n");
                    writer.write("    \"expectedRevenue\": " + rec.getExpectedRevenue() + ",\n");
                    writer.write("    \"trendLabel\": \"" + rec.getTrendLabel() + "\",\n");
                    writer.write("    \"status\": \"" + rec.getStatus() + "\",\n");
                    writer.write("    \"explanation\": \"" + escape(rec.getExplanation()) + "\"\n");
                    writer.write("  }");

                    if (i < recommendations.size() - 1) {
                        writer.write(",");
                    }

                    writer.write("\n");
                }

                writer.write("]\n");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not export price recommendations JSON.", ex);
        }
    }

    /**
     * Exports recommendations csv.
     */
    public void exportRecommendationsCsv(List<PriceRecommendation> recommendations, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("event_id,base_price,recommended_price,premium_seats,forecast_demand,expected_revenue,trend,status,explanation\n");

                // Uses rec for the current item in the loop.
                for (PriceRecommendation rec : recommendations) {
                    writer.write(rec.getEventId() + ",");
                    writer.write(rec.getBasePrice() + ",");
                    writer.write(rec.getRecommendedPrice() + ",");
                    writer.write(rec.getPremiumSeats() + ",");
                    writer.write(rec.getForecastDemand() + ",");
                    writer.write(rec.getExpectedRevenue() + ",");
                    writer.write(rec.getTrendLabel() + ",");
                    writer.write(rec.getStatus() + ",");
                    writer.write("\"" + rec.getExplanation().replace("\"", "'") + "\"\n");
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not export price recommendations CSV.", ex);
        }
    }

    /**
     * Exports simulation csv.
     */
    public void exportSimulationCsv(List<PriceSimulationRow> simulations, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("event_id,candidate_price,predicted_demand,expected_revenue\n");

                // Uses row for the current item in the loop.
                for (PriceSimulationRow row : simulations) {
                    writer.write(row.getEventId() + ",");
                    writer.write(row.getCandidatePrice() + ",");
                    writer.write(row.getPredictedDemand() + ",");
                    writer.write(row.getExpectedRevenue() + "\n");
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not export pricing simulation CSV.", ex);
        }
    }

    /**
     * Exports report.
     */
    public void exportReport(List<PriceRecommendation> recommendations, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write("FestQuant Java OOP Dynamic Pricing Report\n");
                writer.write("=========================================\n\n");

                // Uses rec for the current item in the loop.
                for (PriceRecommendation rec : recommendations) {
                    writer.write("Event ID: " + rec.getEventId() + "\n");
                    writer.write("Base price: Rs " + String.format("%.0f", rec.getBasePrice()) + "\n");
                    writer.write("Recommended price: Rs " + String.format("%.0f", rec.getRecommendedPrice()) + "\n");
                    writer.write("Forecast demand: " + String.format("%.2f", rec.getForecastDemand()) + "\n");
                    writer.write("Expected revenue: Rs " + String.format("%.2f", rec.getExpectedRevenue()) + "\n");
                    writer.write("Trend: " + rec.getTrendLabel() + "\n");
                    writer.write("Status: " + rec.getStatus() + "\n");
                    writer.write("Explanation: " + rec.getExplanation() + "\n\n");
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not export dynamic pricing report.", ex);
        }
    }

    /**
     * Handles the escape step.
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
