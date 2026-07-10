package com.festquant;

import com.festquant.auction.MultiUnitAuction;
import com.festquant.domain.AuctionResult;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiUnitAuctionTest {
    @Test
    void topKBiddersWinAndPayKPlusOneBid() {
        Event event = new Event("E001", "Pro Night", "Music", "V001", 1000, 800, 600, 1400,
                LocalDateTime.of(2026, 8, 10, 20, 0), 3, 1.4);
        List<Bid> bids = List.of(
                bid("B001", "U101", 5000),
                bid("B002", "U102", 4500),
                bid("B003", "U103", 4000),
                bid("B004", "U104", 3500),
                bid("B005", "U105", 3000)
        );

        AuctionResult result = new MultiUnitAuction().runAuction(event, bids, 1200);

        assertEquals(List.of("U101", "U102", "U103"),
                result.getWinners().stream().map(winner -> winner.getUserId()).toList());
        assertEquals(3500, result.getClearingPrice());
        assertEquals(3500, result.getFinalPayment());
    }

    @Test
    void excludesAllBidsBelowReserve() {
        Event event = new Event("E001", "Pro Night", "Music", "V001", 1000, 800, 600, 1400,
                LocalDateTime.of(2026, 8, 10, 20, 0), 3, 1.4);

        AuctionResult result = new MultiUnitAuction().runAuction(
                event, List.of(bid("B001", "U101", 1000)), 1200);

        assertTrue(result.getWinners().isEmpty());
        assertEquals(0, result.getFinalPayment());
    }

    private Bid bid(String bidId, String userId, double amount) {
        return new Bid(bidId, "E001", userId, amount, LocalDateTime.of(2026, 8, 1, 10, 0));
    }
}
