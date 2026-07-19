/**
 * Contains the csv bid repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.Bid;
import com.festquant.exception.DataLoadException;
import com.festquant.service.DataValidationService;
import com.festquant.util.CsvParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads and stores csv bid data.
 */
public class CsvBidRepository implements Repository<Bid> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // Stores the csv path used by this class.
    private final Path csvPath;
    // Stores the validator used by this class.
    private final DataValidationService validator;

    /**
     * Creates a CsvBidRepository with the values needed by this component.
     */
    public CsvBidRepository(Path csvPath, DataValidationService validator) {
        this.csvPath = csvPath;
        this.validator = validator;
    }

    /**
     * Finds bids by event id.
     */
    public List<Bid> findBidsByEventId(String eventId) {
        return findAll().stream().filter(bid -> bid.getEventId().equals(eventId)).toList();
    }

    /**
     * Finds all.
     */
    @Override
    public List<Bid> findAll() {
        Map<String, Bid> latestBidByUserAndEvent = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // Holds the row for this calculation.
                List<String> row = CsvParser.parseLine(line);
                // Holds the bid for this calculation.
                Bid bid = new Bid(
                        row.get(0),
                        row.get(1),
                        row.get(2),
                        Double.parseDouble(row.get(3)),
                        LocalDateTime.parse(row.get(4), FORMATTER)
                );
                validator.validateBid(bid);
                // Holds the key for this calculation.
                String key = bid.getEventId() + ":" + bid.getUserId();
                // Holds the existing for this calculation.
                Bid existing = latestBidByUserAndEvent.get(key);
                if (existing == null || bid.getBidTime().isAfter(existing.getBidTime())) {
                    latestBidByUserAndEvent.put(key, bid);
                }
            }
            return new ArrayList<>(latestBidByUserAndEvent.values());
        } catch (IOException | RuntimeException ex) {
            throw new DataLoadException("Unable to load bids from " + csvPath, ex);
        }
    }

    /**
     * Finds by id.
     */
    @Override
    public Optional<Bid> findById(String id) {
        return findAll().stream().filter(bid -> bid.getBidId().equals(id)).findFirst();
    }
}
