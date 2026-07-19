/**
 * Contains the jdbc auction repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reads and stores jdbc auction data.
 */
public class JdbcAuctionRepository {
    // Stores the jdbc url used by this class.
    private final String jdbcUrl;

    /**
     * Creates a JdbcAuctionRepository with the values needed by this component.
     */
    public JdbcAuctionRepository(String jdbcUrl) {
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
                    CREATE TABLE IF NOT EXISTS auction_results (
                      event_id VARCHAR(10),
                      user_id VARCHAR(10),
                      clearing_price DOUBLE,
                      final_payment DOUBLE
                    )
                    """);
        }
    }

    /**
     * Handles the save step.
     */
    public void save(AuctionResult result) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             // Holds the statement for this calculation.
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO auction_results(event_id, user_id, clearing_price, final_payment)
                     VALUES (?, ?, ?, ?)
                     """)) {
            // Uses winner for the current item in the loop.
            for (AuctionWinner winner : result.getWinners()) {
                statement.setString(1, result.getEventId());
                statement.setString(2, winner.getUserId());
                statement.setDouble(3, result.getClearingPrice());
                statement.setDouble(4, winner.getFinalPayment());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
