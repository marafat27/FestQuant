/**
 * Contains the auction service implementation used by FestQuant.
 */
package com.festquant.service;

import com.festquant.auction.MultiUnitAuction;
import com.festquant.auction.VickreyAuction;
import com.festquant.domain.AuctionResult;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;
import com.festquant.exception.InvalidEventException;
import com.festquant.repository.CsvBidRepository;
import com.festquant.repository.CsvEventRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates the business logic for auction.
 */
public class AuctionService {
    // Stores the event repository used by this class.
    private final CsvEventRepository eventRepository;
    // Stores the bid repository used by this class.
    private final CsvBidRepository bidRepository;
    // Stores the export service used by this class.
    private final ExportService exportService;
    // Stores the multi unit auction used by this class.
    private final MultiUnitAuction multiUnitAuction;
    // Stores the vickrey auction used by this class.
    private final VickreyAuction vickreyAuction;

    /**
     * Creates a AuctionService with the values needed by this component.
     */
    public AuctionService(CsvEventRepository eventRepository, CsvBidRepository bidRepository, ExportService exportService) {
        this.eventRepository = eventRepository;
        this.bidRepository = bidRepository;
        this.exportService = exportService;
        this.multiUnitAuction = new MultiUnitAuction();
        this.vickreyAuction = new VickreyAuction();
    }

    /**
     * Runs auction.
     */
    public AuctionResult runAuction(String eventId) {
        // Holds the event for this calculation.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        // Holds the event bids for this calculation.
        List<Bid> eventBids = bidRepository.findBidsByEventId(eventId);
        // Holds the reserve price for this calculation.
        double reservePrice = calculateReservePrice(event.getBasePrice(), Optional.empty());
        if (event.getPremiumSeats() <= 1) {
            return vickreyAuction.runAuction(event, eventBids, reservePrice);
        }
        return multiUnitAuction.runAuction(event, eventBids, reservePrice);
    }

    /**
     * Runs auction.
     */
    public AuctionResult runAuction(String eventId, double recommendedPrice) {
        // Holds the event for this calculation.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        // Holds the event bids for this calculation.
        List<Bid> eventBids = bidRepository.findBidsByEventId(eventId);
        // Holds the reserve price for this calculation.
        double reservePrice = calculateReservePrice(event.getBasePrice(), Optional.of(recommendedPrice));
        if (event.getPremiumSeats() <= 1) {
            return vickreyAuction.runAuction(event, eventBids, reservePrice);
        }
        return multiUnitAuction.runAuction(event, eventBids, reservePrice);
    }

    /**
     * Runs the single showcase-seat flow used by the admin dashboard while the
     * general multi-unit strategy remains available through runAuction.
     */
    public AuctionResult runVickreyAuction(String eventId, double recommendedPrice) {
        // Holds the event for this calculation.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        // Holds the event bids for this calculation.
        List<Bid> eventBids = bidRepository.findBidsByEventId(eventId);
        // Holds the reserve price for this calculation.
        double reservePrice = calculateReservePrice(
                event.getBasePrice(), Optional.of(recommendedPrice)
        );
        return vickreyAuction.runAuction(event, eventBids, reservePrice);
    }

    /**
     * Strategy selection remains explicit so the OOP design is visible rather
     * than hidden inside framework configuration.
     */
    public String strategyFor(String eventId) {
        // Holds the event for this calculation.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        return event.getPremiumSeats() <= 1
                ? vickreyAuction.getClass().getSimpleName()
                : multiUnitAuction.getClass().getSimpleName();
    }

    /**
     * Exports auction result.
     */
    public Path exportAuctionResult(AuctionResult result, Path outputPath) {
        return exportService.exportAuctionResult(result, outputPath);
    }

    /**
     * Calculates reserve price.
     */
    public double calculateReservePrice(double basePrice, Optional<Double> recommendedPrice) {
        // Holds the base reserve for this calculation.
        double baseReserve = basePrice * 1.5;
        return recommendedPrice
                .map(price -> Math.max(baseReserve, price * 1.25))
                .orElse(baseReserve);
    }
}
