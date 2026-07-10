package com.festquant.auction;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class MultiUnitAuction implements AuctionStrategy {
    @Override
    public AuctionResult runAuction(Event event, List<Bid> bids, double reservePrice) {
        int seats = Math.max(1, event.getPremiumSeats());
        if (bids == null || bids.isEmpty()) {
            return new AuctionResult(event.getEventId(), "MULTI_UNIT_SECOND_PRICE", seats, List.of(),
                    0, reservePrice, 0, "No valid bids were available for this event.");
        }

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

        List<Bid> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            sorted.add(queue.poll());
        }

        if (sorted.isEmpty()) {
            return new AuctionResult(event.getEventId(), "MULTI_UNIT_SECOND_PRICE", seats, List.of(),
                    0, reservePrice, 0, "No bid met the dynamic reserve price.");
        }

        int winnerCount = Math.min(seats, sorted.size());
        double clearingPrice = calculateClearingPrice(sorted, winnerCount, reservePrice);
        double finalPayment = Math.max(clearingPrice, reservePrice);

        List<AuctionWinner> winners = new ArrayList<>();
        for (int i = 0; i < winnerCount; i++) {
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

    public double calculateClearingPrice(List<Bid> sortedBids, int winnerCount, double reservePrice) {
        if (sortedBids.size() > winnerCount) {
            return sortedBids.get(winnerCount).getBidAmount();
        }
        return reservePrice;
    }
}
