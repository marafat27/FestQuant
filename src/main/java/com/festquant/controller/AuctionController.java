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

@RestController
@RequestMapping("/api/auction")
public final class AuctionController {
    private final AnalyticsFacade analytics;

    public AuctionController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @PostMapping("/run/{eventId}")
    public AuctionResult runAuction(@PathVariable("eventId") String eventId) {
        return analytics.runAuction(eventId);
    }

    @PostMapping("/run-vickrey/{eventId}")
    public AuctionResult runVickreyAuction(@PathVariable("eventId") String eventId) {
        return analytics.runVickreyAuction(eventId);
    }

    @GetMapping("/bids/{eventId}")
    public List<AuctionBidView> bids(@PathVariable("eventId") String eventId) {
        return analytics.auctionBids(eventId);
    }

    @GetMapping("/results/{eventId}")
    public AuctionResult getAuctionResult(@PathVariable("eventId") String eventId) {
        return analytics.auctionResult(eventId);
    }
}
