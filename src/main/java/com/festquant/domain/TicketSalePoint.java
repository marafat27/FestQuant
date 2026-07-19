/**
 * Contains the ticket sale point implementation used by FestQuant.
 */
package com.festquant.domain;

import java.time.LocalDateTime;

/**
 * Represents the ticket sale point part of the FestQuant application.
 */
public class TicketSalePoint {
    // Stores the event id used by this class.
    private final String eventId;
    // Stores the timestamp used by this class.
    private final LocalDateTime timestamp;
    // Stores the price at time used by this class.
    private final double priceAtTime;
    // Stores the views used by this class.
    private final int views;
    // Stores the wishlists used by this class.
    private final int wishlists;
    // Stores the tickets sold interval used by this class.
    private final int ticketsSoldInterval;
    // Stores the cumulative sold used by this class.
    private final int cumulativeSold;

    /**
     * Creates a TicketSalePoint with the values needed by this component.
     */
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

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns timestamp.
     */
    public LocalDateTime getTimestamp() { return timestamp; }
    /**
     * Returns price at time.
     */
    public double getPriceAtTime() { return priceAtTime; }
    /**
     * Returns views.
     */
    public int getViews() { return views; }
    /**
     * Returns wishlists.
     */
    public int getWishlists() { return wishlists; }
    /**
     * Returns tickets sold interval.
     */
    public int getTicketsSoldInterval() { return ticketsSoldInterval; }
    /**
     * Returns cumulative sold.
     */
    public int getCumulativeSold() { return cumulativeSold; }
}
