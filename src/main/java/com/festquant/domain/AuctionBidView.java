package com.festquant.domain;

import java.time.LocalDateTime;

/**
 * Admin-facing projection of a sealed bid. Internal IDs remain available to
 * the auction engine but are deliberately omitted from this public view.
 */
public record AuctionBidView(
        String bidderName,
        double bidAmount,
        LocalDateTime bidTime
) {
}
