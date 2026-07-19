/**
 * Contains the json exporter implementation used by FestQuant.
 */
package com.festquant.util;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Represents the json exporter part of the FestQuant application.
 */
public class JsonExporter {
    /**
     * Exports auction result.
     */
    public void exportAuctionResult(AuctionResult result, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, toJson(result));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to export auction result to " + outputPath, ex);
        }
    }

    /**
     * Handles the to json step.
     */
    public String toJson(AuctionResult result) {
        // Holds the json for this calculation.
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendField(json, "eventId", result.getEventId(), true);
        appendField(json, "auctionType", result.getAuctionType(), true);
        appendNumber(json, "premiumSeats", result.getPremiumSeats(), true);
        json.append("  \"winners\": [\n");
        // Holds the winners for this calculation.
        List<AuctionWinner> winners = result.getWinners();
        // Uses i for the current item in the loop.
        for (int i = 0; i < winners.size(); i++) {
            // Holds the winner for this calculation.
            AuctionWinner winner = winners.get(i);
            json.append("    {");
            json.append("\"userId\": \"").append(escape(winner.getUserId())).append("\", ");
            json.append("\"bidderName\": \"").append(escape(winner.getBidderName())).append("\", ");
            json.append("\"bidAmount\": ").append(format(winner.getBidAmount())).append(", ");
            json.append("\"finalPayment\": ").append(format(winner.getFinalPayment()));
            json.append("}");
            if (i < winners.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        appendNumber(json, "clearingPrice", result.getClearingPrice(), true);
        appendNumber(json, "reservePrice", result.getReservePrice(), true);
        appendNumber(json, "finalPayment", result.getFinalPayment(), true);
        appendField(json, "explanation", result.getExplanation(), false);
        json.append("}\n");
        return json.toString();
    }

    /**
     * Handles the append field step.
     */
    private void appendField(StringBuilder json, String name, String value, boolean comma) {
        json.append("  \"").append(name).append("\": \"").append(escape(value)).append("\"");
        json.append(comma ? ",\n" : "\n");
    }

    /**
     * Handles the append number step.
     */
    private void appendNumber(StringBuilder json, String name, double value, boolean comma) {
        json.append("  \"").append(name).append("\": ").append(format(value));
        json.append(comma ? ",\n" : "\n");
    }

    /**
     * Handles the format step.
     */
    private String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value);
    }

    /**
     * Handles the escape step.
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
