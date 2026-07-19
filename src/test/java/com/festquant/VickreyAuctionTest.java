/**
 * Contains the vickrey auction test implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.auction.VickreyAuction;
import com.festquant.domain.AuctionResult;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the expected behaviour of vickrey auction.
 */
class VickreyAuctionTest {
    /**
     * Handles the highest bidder wins and pays second highest bid step.
     */
    @Test
    void highestBidderWinsAndPaysSecondHighestBid() {
        // Holds the event for this calculation.
        Event event = sampleEvent(1);
        // Holds the bids for this calculation.
        List<Bid> bids = List.of(
                bid("B001", "U101", 2500),
                bid("B002", "U102", 2200),
                bid("B003", "U103", 1800)
        );

        // Holds the result for this calculation.
        AuctionResult result = new VickreyAuction().runAuction(event, bids, 1200);

        assertEquals("U101", result.getWinners().get(0).getUserId());
        assertEquals(2200, result.getFinalPayment());
    }

    /**
     * Handles the rejects bids below reserve instead of overcharging winner step.
     */
    @Test
    void rejectsBidsBelowReserveInsteadOfOverchargingWinner() {
        // Holds the result for this calculation.
        AuctionResult result = new VickreyAuction().runAuction(
                sampleEvent(1), List.of(bid("B001", "U101", 1000)), 1200);

        assertTrue(result.getWinners().isEmpty());
        assertEquals(0, result.getFinalPayment());
    }

    /**
     * Handles the sample event step.
     */
    private Event sampleEvent(int seats) {
        return new Event("E001", "Pro Night", "Music", "V001", 1000, 800, 600, 1400,
                LocalDateTime.of(2026, 8, 10, 20, 0), seats, 1.4);
    }

    /**
     * Handles the bid step.
     */
    private Bid bid(String bidId, String userId, double amount) {
        return new Bid(bidId, "E001", userId, amount, LocalDateTime.of(2026, 8, 1, 10, 0));
    }
}
