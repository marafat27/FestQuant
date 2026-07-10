package com.festquant.domain;

import java.util.Collections;
import java.util.List;

public class AuctionResult {
    private final String eventId;
    private final String auctionType;
    private final int premiumSeats;
    private final List<AuctionWinner> winners;
    private final double clearingPrice;
    private final double reservePrice;
    private final double finalPayment;
    private final String explanation;

    public AuctionResult(String eventId, String auctionType, int premiumSeats, List<AuctionWinner> winners,
                         double clearingPrice, double reservePrice, double finalPayment, String explanation) {
        this.eventId = eventId;
        this.auctionType = auctionType;
        this.premiumSeats = premiumSeats;
        this.winners = List.copyOf(winners);
        this.clearingPrice = clearingPrice;
        this.reservePrice = reservePrice;
        this.finalPayment = finalPayment;
        this.explanation = explanation;
    }

    public String getEventId() { return eventId; }
    public String getAuctionType() { return auctionType; }
    public int getPremiumSeats() { return premiumSeats; }
    public List<AuctionWinner> getWinners() { return Collections.unmodifiableList(winners); }
    public double getClearingPrice() { return clearingPrice; }
    public double getReservePrice() { return reservePrice; }
    public double getFinalPayment() { return finalPayment; }
    public String getExplanation() { return explanation; }
}
