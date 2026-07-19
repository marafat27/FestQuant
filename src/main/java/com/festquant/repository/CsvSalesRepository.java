/**
 * Contains the csv sales repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.TicketSalePoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Reads and stores csv sales data.
 */
public class CsvSalesRepository {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // Stores the csv path used by this class.
    private final Path csvPath;

    /**
     * Creates a CsvSalesRepository with the values needed by this component.
     */
    public CsvSalesRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Finds all.
     */
    public List<TicketSalePoint> findAll() {
        List<Map<String, String>> rows = readRows();
        // Holds the sales for this calculation.
        List<TicketSalePoint> sales = new ArrayList<>();

        for (Map<String, String> row : rows) {
            // Holds the point for this calculation.
            TicketSalePoint point = new TicketSalePoint(
                    get(row, "event_id", "eventId"),
                    LocalDateTime.parse(get(row, "timestamp", "time"), FORMATTER),
                    parseDouble(row, 0.0, "price_at_time", "priceAtTime"),
                    parseInt(row, 0, "views"),
                    parseInt(row, 0, "wishlists"),
                    parseInt(row, 0, "tickets_sold_interval", "ticketsSoldInterval"),
                    parseInt(row, 0, "cumulative_sold", "cumulativeSold")
            );

            sales.add(point);
        }

        return sales;
    }

    public Map<String, List<TicketSalePoint>> groupByEvent() {
        Map<String, List<TicketSalePoint>> groupedSales = new HashMap<>();

        // Uses point for the current item in the loop.
        for (TicketSalePoint point : findAll()) {
            groupedSales.computeIfAbsent(point.getEventId(), key -> new ArrayList<>()).add(point);
        }

        return groupedSales;
    }

    private List<Map<String, String>> readRows() {
        if (!Files.exists(csvPath)) {
            throw new RuntimeException("Missing sales CSV: " + csvPath);
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
            throw new RuntimeException("Could not read sales CSV.", ex);
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
