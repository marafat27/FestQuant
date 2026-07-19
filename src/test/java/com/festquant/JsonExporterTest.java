/**
 * Contains the json exporter test implementation used by FestQuant.
 */
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

/**
 * Checks the expected behaviour of json exporter.
 */
class JsonExporterTest {
    /**
     * Creates s auction result json file.
     */
    @Test
    void createsAuctionResultJsonFile() throws Exception {
        // Holds the event for this calculation.
        Event event = new Event("E001", "Pro Night", "Music", "V001", 1000, 800, 600, 1400,
                LocalDateTime.of(2026, 8, 10, 20, 0), 2, 1.4);
        // Holds the result for this calculation.
        AuctionResult result = new MultiUnitAuction().runAuction(event, List.of(
                new Bid("B001", "E001", "U101", 2500, LocalDateTime.now()),
                new Bid("B002", "E001", "U102", 2200, LocalDateTime.now()),
                new Bid("B003", "E001", "U103", 1800, LocalDateTime.now())
        ), 1200);
        // Holds the output for this calculation.
        Path output = Path.of("data/output/auction_results_test.json");

        new JsonExporter().exportAuctionResult(result, output);

        assertTrue(Files.exists(output));
        assertTrue(Files.readString(output).contains("\"auctionType\""));
    }
}
