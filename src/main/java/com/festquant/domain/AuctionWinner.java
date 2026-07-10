package com.festquant.domain;

public class AuctionWinner {
    private final String userId;
    private final String bidderName;
    private final double bidAmount;
    private final double finalPayment;

    public AuctionWinner(String userId, double bidAmount, double finalPayment) {
        this(userId, userId, bidAmount, finalPayment);
    }

    public AuctionWinner(String userId, String bidderName, double bidAmount, double finalPayment) {
        this.userId = userId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.finalPayment = finalPayment;
    }

    public String getUserId() { return userId; }
    public String getBidderName() { return bidderName; }
    public double getBidAmount() { return bidAmount; }
    public double getFinalPayment() { return finalPayment; }
}
