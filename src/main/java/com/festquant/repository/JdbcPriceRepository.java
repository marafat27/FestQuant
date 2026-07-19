/**
 * Contains the jdbc price repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.PriceRecommendation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reads and stores jdbc price data.
 */
public class JdbcPriceRepository {
    // Stores the jdbc url used by this class.
    private final String jdbcUrl;

    /**
     * Creates a JdbcPriceRepository with the values needed by this component.
     */
    public JdbcPriceRepository(String jdbcUrl) {
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
                    CREATE TABLE IF NOT EXISTS price_recommendations (
                      event_id VARCHAR(10) PRIMARY KEY,
                      base_price DOUBLE,
                      recommended_price DOUBLE,
                      expected_revenue DOUBLE
                    )
                    """);
        }
    }

    /**
     * Handles the save step.
     */
    public void save(PriceRecommendation recommendation) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             // Holds the statement for this calculation.
             PreparedStatement statement = connection.prepareStatement("""
                     MERGE INTO price_recommendations KEY(event_id)
                     VALUES (?, ?, ?, ?)
                     """)) {
            statement.setString(1, recommendation.getEventId());
            statement.setDouble(2, recommendation.getBasePrice());
            statement.setDouble(3, recommendation.getRecommendedPrice());
            statement.setDouble(4, recommendation.getExpectedRevenue());
            statement.executeUpdate();
        }
    }
}
