package com.festquant;

import com.festquant.auction.MultiUnitAuction;
import com.festquant.domain.AuctionResult;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;
import com.festquant.util.JsonExporter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExporterTest {
    @Test
    void createsAuctionResultJsonFile() throws Exception {
        Event event = new Event("E001", "Pro Night", "Music", "V001", 1000, 800, 600, 1400,
                LocalDateTime.of(2026, 8, 10, 20, 0), 2, 1.4);
        AuctionResult result = new MultiUnitAuction().runAuction(event, List.of(
                new Bid("B001", "E001", "U101", 2500, LocalDateTime.now()),
                new Bid("B002", "E001", "U102", 2200, LocalDateTime.now()),
                new Bid("B003", "E001", "U103", 1800, LocalDateTime.now())
        ), 1200);
        Path output = Path.of("data/output/auction_results_test.json");

        new JsonExporter().exportAuctionResult(result, output);

        assertTrue(Files.exists(output));
        assertTrue(Files.readString(output).contains("\"auctionType\""));
    }
}
