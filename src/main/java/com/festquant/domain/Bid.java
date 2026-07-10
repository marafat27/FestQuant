package com.festquant.domain;

import java.time.LocalDateTime;

public class Bid {
    private final String bidId;
    private final String eventId;
    private final String userId;
    private final double bidAmount;
    private final LocalDateTime bidTime;

    public Bid(String bidId, String eventId, String userId, double bidAmount, LocalDateTime bidTime) {
        this.bidId = bidId;
        this.eventId = eventId;
        this.userId = userId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    public String getBidId() { return bidId; }
    public String getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getBidTime() { return bidTime; }
}
