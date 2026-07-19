/**
 * Contains the csv pricing event repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.PricingEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Reads and stores csv pricing event data.
 */
public class CsvPricingEventRepository {
    // Stores the csv path used by this class.
    private final Path csvPath;

    /**
     * Creates a CsvPricingEventRepository with the values needed by this component.
     */
    public CsvPricingEventRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Finds all.
     */
    public List<PricingEvent> findAll() {
        List<Map<String, String>> rows = readRows();
        // Holds the events for this calculation.
        List<PricingEvent> events = new ArrayList<>();

        for (Map<String, String> row : rows) {
            // Holds the event for this calculation.
            PricingEvent event = new PricingEvent(
                    get(row, "event_id", "eventId", "id"),
                    get(row, "event_name", "eventName", "name"),
                    get(row, "event_datetime", "eventDatetime", "datetime", "date_time"),
                    parseDouble(row, 500.0, "base_price", "basePrice", "price"),
                    parseDouble(row, 300.0, "min_price", "minPrice"),
                    parseDouble(row, 1000.0, "max_price", "maxPrice"),
                    parseInt(row, 500, "capacity", "total_seats"),
                    parseInt(row, 20, "premium_seats", "premiumSeats"),
                    parseDouble(row, 0.50, "slot_popularity", "slotPopularity")
            );

            events.add(event);
        }

        return events;
    }

    private List<Map<String, String>> readRows() {
        if (!Files.exists(csvPath)) {
            throw new RuntimeException("Missing events CSV: " + csvPath);
        }

        try {
            // Holds the lines for this calculation.
            List<String> lines = Files.readAllLines(csvPath);

            if (lines.size() <= 1) {
                return new ArrayList<>();
            }

            // Holds the headers for this calculation.
            String[] headers = splitCsv(lines.get(0));
            List<Map<String, String>> rows = new ArrayList<>();

            // Uses i for the current item in the loop.
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) {
                    continue;
                }

                // Holds the values for this calculation.
                String[] values = splitCsv(lines.get(i));
                Map<String, String> row = new HashMap<>();

                // Uses j for the current item in the loop.
                for (int j = 0; j < headers.length && j < values.length; j++) {
                    row.put(headers[j].trim(), values[j].trim());
                }

                rows.add(row);
            }

            return rows;
        } catch (IOException ex) {
            throw new RuntimeException("Could not read events CSV.", ex);
        }
    }

    /**
     * Handles the get step.
     */
    private String get(Map<String, String> row, String... keys) {
        // Uses key for the current item in the loop.
        for (String key : keys) {
            if (row.containsKey(key) && !row.get(key).isEmpty()) {
                return row.get(key);
            }
        }

        return "";
    }

    /**
     * Handles the parse int step.
     */
    private int parseInt(Map<String, String> row, int defaultValue, String... keys) {
        // Holds the value for this calculation.
        String value = get(row, keys);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return (int) Math.round(Double.parseDouble(value));
    }

    /**
     * Handles the parse double step.
     */
    private double parseDouble(Map<String, String> row, double defaultValue, String... keys) {
        // Holds the value for this calculation.
        String value = get(row, keys);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return Double.parseDouble(value);
    }

    /**
     * Handles the split csv step.
     */
    private String[] splitCsv(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
