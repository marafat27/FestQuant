package com.festquant.repository;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcAuctionRepository {
    private final String jdbcUrl;

    public JdbcAuctionRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
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

    public void save(AuctionResult result) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO auction_results(event_id, user_id, clearing_price, final_payment)
                     VALUES (?, ?, ?, ?)
                     """)) {
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
