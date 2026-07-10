package com.festquant.util;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JsonExporter {
    public void exportAuctionResult(AuctionResult result, Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, toJson(result));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to export auction result to " + outputPath, ex);
        }
    }

    public String toJson(AuctionResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendField(json, "eventId", result.getEventId(), true);
        appendField(json, "auctionType", result.getAuctionType(), true);
        appendNumber(json, "premiumSeats", result.getPremiumSeats(), true);
        json.append("  \"winners\": [\n");
        List<AuctionWinner> winners = result.getWinners();
        for (int i = 0; i < winners.size(); i++) {
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

    private void appendField(StringBuilder json, String name, String value, boolean comma) {
        json.append("  \"").append(name).append("\": \"").append(escape(value)).append("\"");
        json.append(comma ? ",\n" : "\n");
    }

    private void appendNumber(StringBuilder json, String name, double value, boolean comma) {
        json.append("  \"").append(name).append("\": ").append(format(value));
        json.append(comma ? ",\n" : "\n");
    }

    private String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
