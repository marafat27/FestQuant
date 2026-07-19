/**
 * Contains the jdbc event repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.Event;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and stores jdbc event data.
 */
public class JdbcEventRepository {
    // Stores the jdbc url used by this class.
    private final String jdbcUrl;

    /**
     * Creates a JdbcEventRepository with the values needed by this component.
     */
    public JdbcEventRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Creates table.
     */
    public void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             // Holds the statement for this calculation.
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS events (
                      event_id VARCHAR(10) PRIMARY KEY,
                      event_name VARCHAR(100),
                      category VARCHAR(50),
                      capacity INT,
                      base_price DOUBLE
                    )
                    """);
        }
    }

    /**
     * Handles the save step.
     */
    public void save(Event event) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             // Holds the statement for this calculation.
             PreparedStatement statement = connection.prepareStatement("""
                     MERGE INTO events KEY(event_id)
                     VALUES (?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, event.getEventId());
            statement.setString(2, event.getEventName());
            statement.setString(3, event.getCategory());
            statement.setInt(4, event.getCapacity());
            statement.setDouble(5, event.getBasePrice());
            statement.executeUpdate();
        }
    }

    /**
     * Finds all basic events.
     */
    public List<Event> findAllBasicEvents() throws SQLException {
        // Holds the events for this calculation.
        List<Event> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             // Holds the statement for this calculation.
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM events");
             // Holds the result set for this calculation.
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                events.add(new Event(
                        resultSet.getString("event_id"),
                        resultSet.getString("event_name"),
                        resultSet.getString("category"),
                        "V000",
                        resultSet.getInt("capacity"),
                        resultSet.getDouble("base_price"),
                        resultSet.getDouble("base_price") * 0.75,
                        resultSet.getDouble("base_price") * 1.75,
                        LocalDateTime.now(),
                        1,
                        1.0
                ));
            }
        }
        return events;
    }
}
