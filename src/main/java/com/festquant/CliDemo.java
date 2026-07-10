package com.festquant;

import com.festquant.domain.AuctionResult;
import com.festquant.repository.CsvBidRepository;
import com.festquant.repository.CsvEventRepository;
import com.festquant.service.AuctionService;
import com.festquant.service.DataValidationService;
import com.festquant.service.ExportService;
import com.festquant.util.JsonExporter;

import java.nio.file.Path;

public class CliDemo {
    public static void main(String[] args) {
        Path projectRoot = Path.of("").toAbsolutePath();
        AuctionService auctionService = new AuctionService(
                new CsvEventRepository(projectRoot.resolve("data/input/events.csv")),
                new CsvBidRepository(projectRoot.resolve("data/input/premium_bids.csv"), new DataValidationService()),
                new ExportService(new JsonExporter())
        );
        String eventId = args.length > 0 ? args[0] : "E001";
        AuctionResult result = auctionService.runAuction(eventId);
        Path outputPath = projectRoot.resolve("data/output/auction_results.json");
        auctionService.exportAuctionResult(result, outputPath);
        System.out.println("Auction complete for " + eventId);
        System.out.println("Winners: " + result.getWinners().stream().map(winner -> winner.getUserId()).toList());
        System.out.println("Final payment: " + result.getFinalPayment());
        System.out.println("Exported: " + outputPath);
    }
}
