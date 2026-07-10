package com.festquant.repository;

import com.festquant.domain.TicketSalePoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvSalesRepository {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Path csvPath;

    public CsvSalesRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public List<TicketSalePoint> findAll() {
        List<Map<String, String>> rows = readRows();
        List<TicketSalePoint> sales = new ArrayList<>();

        for (Map<String, String> row : rows) {
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
            List<String> lines = Files.readAllLines(csvPath);

            if (lines.size() <= 1) {
                return new ArrayList<>();
            }

            String[] headers = splitCsv(lines.get(0));
            List<Map<String, String>> rows = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().isEmpty()) {
                    continue;
                }

                String[] values = splitCsv(lines.get(i));
                Map<String, String> row = new HashMap<>();

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

    private String get(Map<String, String> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key) && !row.get(key).isEmpty()) {
                return row.get(key);
            }
        }

        return "";
    }

    private int parseInt(Map<String, String> row, int defaultValue, String... keys) {
        String value = get(row, keys);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return (int) Math.round(Double.parseDouble(value));
    }

    private double parseDouble(Map<String, String> row, double defaultValue, String... keys) {
        String value = get(row, keys);

        if (value.isEmpty()) {
            return defaultValue;
        }

        return Double.parseDouble(value);
    }

    private String[] splitCsv(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
