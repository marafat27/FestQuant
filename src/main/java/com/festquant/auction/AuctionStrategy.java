/**
 * Contains the auction strategy implementation used by FestQuant.
 */
package com.festquant.auction;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;

import java.util.List;

/**
 * Defines the operations required for auction strategy.
 */
public interface AuctionStrategy {
    /**
     * Runs auction.
     */
    AuctionResult runAuction(Event event, List<Bid> bids, double reservePrice);
}
