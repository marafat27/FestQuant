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

public class AuctionService {
    private final CsvEventRepository eventRepository;
    private final CsvBidRepository bidRepository;
    private final ExportService exportService;
    private final MultiUnitAuction multiUnitAuction;
    private final VickreyAuction vickreyAuction;

    public AuctionService(CsvEventRepository eventRepository, CsvBidRepository bidRepository, ExportService exportService) {
        this.eventRepository = eventRepository;
        this.bidRepository = bidRepository;
        this.exportService = exportService;
        this.multiUnitAuction = new MultiUnitAuction();
        this.vickreyAuction = new VickreyAuction();
    }

    public AuctionResult runAuction(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        List<Bid> eventBids = bidRepository.findBidsByEventId(eventId);
        double reservePrice = calculateReservePrice(event.getBasePrice(), Optional.empty());
        if (event.getPremiumSeats() <= 1) {
            return vickreyAuction.runAuction(event, eventBids, reservePrice);
        }
        return multiUnitAuction.runAuction(event, eventBids, reservePrice);
    }

    public AuctionResult runAuction(String eventId, double recommendedPrice) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        List<Bid> eventBids = bidRepository.findBidsByEventId(eventId);
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
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        List<Bid> eventBids = bidRepository.findBidsByEventId(eventId);
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
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidEventException("Event not found: " + eventId));
        return event.getPremiumSeats() <= 1
                ? vickreyAuction.getClass().getSimpleName()
                : multiUnitAuction.getClass().getSimpleName();
    }

    public Path exportAuctionResult(AuctionResult result, Path outputPath) {
        return exportService.exportAuctionResult(result, outputPath);
    }

    public double calculateReservePrice(double basePrice, Optional<Double> recommendedPrice) {
        double baseReserve = basePrice * 1.5;
        return recommendedPrice
                .map(price -> Math.max(baseReserve, price * 1.25))
                .orElse(baseReserve);
    }
}
