/**
 * Contains the auction result implementation used by FestQuant.
 */
package com.festquant.domain;

import java.util.Collections;
import java.util.List;

/**
 * Represents the auction result part of the FestQuant application.
 */
public class AuctionResult {
    // Stores the event id used by this class.
    private final String eventId;
    // Stores the auction type used by this class.
    private final String auctionType;
    // Stores the premium seats used by this class.
    private final int premiumSeats;
    // Stores the winners used by this class.
    private final List<AuctionWinner> winners;
    // Stores the clearing price used by this class.
    private final double clearingPrice;
    // Stores the reserve price used by this class.
    private final double reservePrice;
    // Stores the final payment used by this class.
    private final double finalPayment;
    // Stores the explanation used by this class.
    private final String explanation;

    /**
     * Creates a AuctionResult with the values needed by this component.
     */
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

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns auction type.
     */
    public String getAuctionType() { return auctionType; }
    /**
     * Returns premium seats.
     */
    public int getPremiumSeats() { return premiumSeats; }
    /**
     * Returns winners.
     */
    public List<AuctionWinner> getWinners() { return Collections.unmodifiableList(winners); }
    /**
     * Returns clearing price.
     */
    public double getClearingPrice() { return clearingPrice; }
    /**
     * Returns reserve price.
     */
    public double getReservePrice() { return reservePrice; }
    /**
     * Returns final payment.
     */
    public double getFinalPayment() { return finalPayment; }
    /**
     * Returns explanation.
     */
    public String getExplanation() { return explanation; }
}
