package com.festquant.domain;

import java.time.LocalDateTime;

public class TicketSalePoint {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final double priceAtTime;
    private final int views;
    private final int wishlists;
    private final int ticketsSoldInterval;
    private final int cumulativeSold;

    public TicketSalePoint(String eventId, LocalDateTime timestamp, double priceAtTime, int views,
                           int wishlists, int ticketsSoldInterval, int cumulativeSold) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.priceAtTime = priceAtTime;
        this.views = views;
        this.wishlists = wishlists;
        this.ticketsSoldInterval = ticketsSoldInterval;
        this.cumulativeSold = cumulativeSold;
    }

    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getPriceAtTime() { return priceAtTime; }
    public int getViews() { return views; }
    public int getWishlists() { return wishlists; }
    public int getTicketsSoldInterval() { return ticketsSoldInterval; }
    public int getCumulativeSold() { return cumulativeSold; }
}
