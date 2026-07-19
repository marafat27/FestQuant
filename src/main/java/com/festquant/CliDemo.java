/**
 * Contains the cli demo implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.domain.AuctionResult;
import com.festquant.repository.CsvBidRepository;
import com.festquant.repository.CsvEventRepository;
import com.festquant.service.AuctionService;
import com.festquant.service.DataValidationService;
import com.festquant.service.ExportService;
import com.festquant.util.JsonExporter;

import java.nio.file.Path;

/**
 * Represents the cli demo part of the FestQuant application.
 */
public class CliDemo {
    /**
     * Handles the main step.
     */
    public static void main(String[] args) {
        // Holds the project root for this calculation.
        Path projectRoot = Path.of("").toAbsolutePath();
        // Holds the auction service for this calculation.
        AuctionService auctionService = new AuctionService(
                new CsvEventRepository(projectRoot.resolve("data/input/events.csv")),
                new CsvBidRepository(projectRoot.resolve("data/input/premium_bids.csv"), new DataValidationService()),
                new ExportService(new JsonExporter())
        );
        // Holds the event id for this calculation.
        String eventId = args.length > 0 ? args[0] : "E001";
        // Holds the result for this calculation.
        AuctionResult result = auctionService.runAuction(eventId);
        // Holds the output path for this calculation.
        Path outputPath = projectRoot.resolve("data/output/auction_results.json");
        auctionService.exportAuctionResult(result, outputPath);
        System.out.println("Auction complete for " + eventId);
        System.out.println("Winners: " + result.getWinners().stream().map(winner -> winner.getUserId()).toList());
        System.out.println("Final payment: " + result.getFinalPayment());
        System.out.println("Exported: " + outputPath);
    }
}
