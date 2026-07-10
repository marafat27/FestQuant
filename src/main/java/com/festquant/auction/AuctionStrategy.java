package com.festquant.auction;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;

import java.util.List;

public interface AuctionStrategy {
    AuctionResult runAuction(Event event, List<Bid> bids, double reservePrice);
}
