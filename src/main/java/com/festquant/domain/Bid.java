/**
 * Contains the bid implementation used by FestQuant.
 */
package com.festquant.domain;

import java.time.LocalDateTime;

/**
 * Represents the bid part of the FestQuant application.
 */
public class Bid {
    // Stores the bid id used by this class.
    private final String bidId;
    // Stores the event id used by this class.
    private final String eventId;
    // Stores the user id used by this class.
    private final String userId;
    // Stores the bid amount used by this class.
    private final double bidAmount;
    // Stores the bid time used by this class.
    private final LocalDateTime bidTime;

    /**
     * Creates a Bid with the values needed by this component.
     */
    public Bid(String bidId, String eventId, String userId, double bidAmount, LocalDateTime bidTime) {
        this.bidId = bidId;
        this.eventId = eventId;
        this.userId = userId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    /**
     * Returns bid id.
     */
    public String getBidId() { return bidId; }
    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns user id.
     */
    public String getUserId() { return userId; }
    /**
     * Returns bid amount.
     */
    public double getBidAmount() { return bidAmount; }
    /**
     * Returns bid time.
     */
    public LocalDateTime getBidTime() { return bidTime; }
}
