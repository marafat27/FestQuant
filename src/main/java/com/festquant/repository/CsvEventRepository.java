/**
 * Contains the csv event repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.Event;
import com.festquant.exception.DataLoadException;
import com.festquant.util.CsvParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads and stores csv event data.
 */
public class CsvEventRepository implements Repository<Event> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // Stores the csv path used by this class.
    private final Path csvPath;

    /**
     * Creates a CsvEventRepository with the values needed by this component.
     */
    public CsvEventRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Finds all events.
     */
    public List<Event> findAllEvents() {
        return findAll();
    }

    /**
     * Finds all.
     */
    @Override
    public List<Event> findAll() {
        // Holds the events for this calculation.
        List<Event> events = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // Holds the row for this calculation.
                List<String> row = CsvParser.parseLine(line);
                events.add(new Event(
                        row.get(0),
                        row.get(1),
                        row.get(2),
                        row.get(3),
                        Integer.parseInt(row.get(4)),
                        Double.parseDouble(row.get(5)),
                        Double.parseDouble(row.get(6)),
                        Double.parseDouble(row.get(7)),
                        LocalDateTime.parse(row.get(8), FORMATTER),
                        Integer.parseInt(row.get(9)),
                        Double.parseDouble(row.get(10))
                ));
            }
            return events;
        } catch (IOException | RuntimeException ex) {
            throw new DataLoadException("Unable to load events from " + csvPath, ex);
        }
    }

    /**
     * Finds by id.
     */
    @Override
    public Optional<Event> findById(String id) {
        return findAll().stream().filter(event -> event.getEventId().equals(id)).findFirst();
    }
}
