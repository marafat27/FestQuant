package com.festquant.repository;

import com.festquant.domain.PriceRecommendation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcPriceRepository {
    private final String jdbcUrl;

    public JdbcPriceRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
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

    public void save(PriceRecommendation recommendation) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
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
