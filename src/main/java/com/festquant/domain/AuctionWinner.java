/**
 * Contains the auction winner implementation used by FestQuant.
 */
package com.festquant.domain;

/**
 * Represents the auction winner part of the FestQuant application.
 */
public class AuctionWinner {
    // Stores the user id used by this class.
    private final String userId;
    // Stores the bidder name used by this class.
    private final String bidderName;
    // Stores the bid amount used by this class.
    private final double bidAmount;
    // Stores the final payment used by this class.
    private final double finalPayment;

    /**
     * Creates a AuctionWinner with the values needed by this component.
     */
    public AuctionWinner(String userId, double bidAmount, double finalPayment) {
        this(userId, userId, bidAmount, finalPayment);
    }

    /**
     * Creates a AuctionWinner with the values needed by this component.
     */
    public AuctionWinner(String userId, String bidderName, double bidAmount, double finalPayment) {
        this.userId = userId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.finalPayment = finalPayment;
    }

    /**
     * Returns user id.
     */
    public String getUserId() { return userId; }
    /**
     * Returns bidder name.
     */
    public String getBidderName() { return bidderName; }
    /**
     * Returns bid amount.
     */
    public double getBidAmount() { return bidAmount; }
    /**
     * Returns final payment.
     */
    public double getFinalPayment() { return finalPayment; }
}
