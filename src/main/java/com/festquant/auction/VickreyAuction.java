/**
 * Contains the vickrey auction implementation used by FestQuant.
 */
package com.festquant.auction;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionWinner;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;

import java.util.Comparator;
import java.util.List;

/**
 * Allocates one premium seat with a Vickrey, or second-price, auction.
 * The highest eligible bid wins, but the payment is based on the next bid.
 */
public class VickreyAuction implements AuctionStrategy {
    /**
     * Filters bids against the reserve, applies deterministic tie-breaking and
     * returns the winner with the amount that must be paid.
     */
    @Override
    public AuctionResult runAuction(Event event, List<Bid> bids, double reservePrice) {
        if (bids == null || bids.isEmpty()) {
            return new AuctionResult(event.getEventId(), "SINGLE_SEAT_SECOND_PRICE", 1, List.of(),
                    0, reservePrice, 0, "No valid bids were available for this event.");
        }

        // Eligible bids are ordered by amount, then time, then user ID for a stable tie-break.
        List<Bid> sorted = bids.stream()
                .filter(bid -> bid.getBidAmount() >= reservePrice)
                .sorted(Comparator.comparingDouble(Bid::getBidAmount).reversed()
                        .thenComparing(Bid::getBidTime)
                        .thenComparing(Bid::getUserId))
                .toList();

        if (sorted.isEmpty()) {
            return new AuctionResult(event.getEventId(), "SINGLE_SEAT_SECOND_PRICE", 1, List.of(),
                    0, reservePrice, 0, "No bid met the dynamic reserve price.");
        }

        // The first bid is the highest eligible bid and therefore wins the seat.
        Bid winner = sorted.get(0);
        // If there is no second bid, the reserve acts as the fallback price.
        double secondHighest = sorted.size() > 1 ? sorted.get(1).getBidAmount() : reservePrice;
        // Vickrey payment equation: payment = max(second-highest bid, reserve price).
        double finalPayment = Math.max(secondHighest, reservePrice);

        return new AuctionResult(
                event.getEventId(),
                "SINGLE_SEAT_SECOND_PRICE",
                1,
                List.of(new AuctionWinner(winner.getUserId(), winner.getBidAmount(), finalPayment)),
                secondHighest,
                reservePrice,
                finalPayment,
                "Highest eligible bidder wins one premium seat and pays the second-highest eligible bid or the dynamic reserve price, whichever is higher. This second-price rule makes truthful bidding rational."
        );
    }
}
