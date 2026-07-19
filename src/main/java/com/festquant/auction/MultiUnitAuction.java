/**
 * Contains the multi unit auction implementation used by FestQuant.
 */
package com.festquant.auction;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Allocates several premium seats with a uniform-price auction. The top bids
 * win and every winner pays the same clearing price.
 */
public class MultiUnitAuction implements AuctionStrategy {
    /**
     * Runs the multi-seat auction after removing bids below the reserve price.
     */
    @Override
    public AuctionResult runAuction(Event event, List<Bid> bids, double reservePrice) {
        // Number of premium seats available; at least one seat is always considered.
        int seats = Math.max(1, event.getPremiumSeats());
        if (bids == null || bids.isEmpty()) {
            return new AuctionResult(event.getEventId(), "MULTI_UNIT_SECOND_PRICE", seats, List.of(),
                    0, reservePrice, 0, "No valid bids were available for this event.");
        }

        // Priority queue keeps the strongest bid at the front with stable tie-breaking.
        PriorityQueue<Bid> queue = new PriorityQueue<>(
                Comparator.comparingDouble(Bid::getBidAmount).reversed()
                        .thenComparing(Bid::getBidTime)
                        .thenComparing(Bid::getUserId)
        );
        // A bidder must never be charged more than their own bid. Filtering at
        // the reserve preserves individual rationality in the auction.
        bids.stream()
                .filter(bid -> bid.getBidAmount() >= reservePrice)
                .forEach(queue::add);

        // Eligible bids copied into descending order for winner selection.
        List<Bid> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            sorted.add(queue.poll());
        }

        if (sorted.isEmpty()) {
            return new AuctionResult(event.getEventId(), "MULTI_UNIT_SECOND_PRICE", seats, List.of(),
                    0, reservePrice, 0, "No bid met the dynamic reserve price.");
        }

        // Winners cannot exceed either the seat count or the number of eligible bids.
        int winnerCount = Math.min(seats, sorted.size());
        // Clearing price is the first losing bid when one exists.
        double clearingPrice = calculateClearingPrice(sorted, winnerCount, reservePrice);
        // Uniform payment equation: payment = max(first losing bid, reserve price).
        double finalPayment = Math.max(clearingPrice, reservePrice);

        // Each selected bidder receives one seat and pays the same final amount.
        List<AuctionWinner> winners = new ArrayList<>();
        // Uses i for the current item in the loop.
        for (int i = 0; i < winnerCount; i++) {
            // Holds the bid for this calculation.
            Bid bid = sorted.get(i);
            winners.add(new AuctionWinner(bid.getUserId(), bid.getBidAmount(), finalPayment));
        }

        return new AuctionResult(
                event.getEventId(),
                "MULTI_UNIT_SECOND_PRICE",
                seats,
                winners,
                clearingPrice,
                reservePrice,
                finalPayment,
                "Top " + winnerCount + " eligible bidders won premium seats. Bids below the dynamic reserve were excluded. Each winner pays the next-highest losing bid or the reserve price, which follows a uniform-price second-price auction."
        );
    }

    /**
     * Returns the first losing bid, or the reserve when every eligible bidder wins.
     */
    public double calculateClearingPrice(List<Bid> sortedBids, int winnerCount, double reservePrice) {
        if (sortedBids.size() > winnerCount) {
            return sortedBids.get(winnerCount).getBidAmount();
        }
        return reservePrice;
    }
}
