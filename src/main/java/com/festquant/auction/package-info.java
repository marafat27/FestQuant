/**
 * Auction algorithms for allocating premium festival seats.
 *
 * <p>The package demonstrates the Strategy pattern: each auction type exposes
 * the same {@link com.festquant.auction.AuctionStrategy} contract, while
 * {@link com.festquant.auction.VickreyAuction} and
 * {@link com.festquant.auction.MultiUnitAuction} implement different economic
 * rules. This keeps auction policy separate from controllers, repositories and
 * UI code.</p>
 */
package com.festquant.auction;
