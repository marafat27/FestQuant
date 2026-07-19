/**
 * Contains the auction controller implementation used by FestQuant.
 */
package com.festquant.controller;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionBidView;
import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes HTTP endpoints for auction operations.
 */
@RestController
@RequestMapping("/api/auction")
public final class AuctionController {
    // Stores the analytics used by this class.
    private final AnalyticsFacade analytics;

    /**
     * Creates a AuctionController with the values needed by this component.
     */
    public AuctionController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    /**
     * Runs auction.
     */
    @PostMapping("/run/{eventId}")
    public AuctionResult runAuction(@PathVariable("eventId") String eventId) {
        return analytics.runAuction(eventId);
    }

    /**
     * Runs vickrey auction.
     */
    @PostMapping("/run-vickrey/{eventId}")
    public AuctionResult runVickreyAuction(@PathVariable("eventId") String eventId) {
        return analytics.runVickreyAuction(eventId);
    }

    /**
     * Handles the bids step.
     */
    @GetMapping("/bids/{eventId}")
    public List<AuctionBidView> bids(@PathVariable("eventId") String eventId) {
        return analytics.auctionBids(eventId);
    }

    /**
     * Returns auction result.
     */
    @GetMapping("/results/{eventId}")
    public AuctionResult getAuctionResult(@PathVariable("eventId") String eventId) {
        return analytics.auctionResult(eventId);
    }
}
