package com.festquant.domain;

public class PricingEvent {
    private final String eventId;              // Unique event ID.
    private final String eventName;            // Event name for reports.
    private final String eventDatetime;        // Event date-time string.
    private final double basePrice;            // Original fixed price.
    private final double minPrice;             // Lower allowed price.
    private final double maxPrice;             // Upper allowed price.
    private final int capacity;                // Total seats.
    private final int premiumSeats;            // Seats reserved for auction.
    private final double slotPopularity;       // Timing popularity score.

    public PricingEvent(
            String eventId,
            String eventName,
            String eventDatetime,
            double basePrice,
            double minPrice,
            double maxPrice,
            int capacity,
            int premiumSeats,
            double slotPopularity
    ) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDatetime = eventDatetime;
        this.basePrice = basePrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.capacity = capacity;
        this.premiumSeats = premiumSeats;
        this.slotPopularity = slotPopularity;
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getEventDatetime() { return eventDatetime; }
    public double getBasePrice() { return basePrice; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }
    public int getCapacity() { return capacity; }
    public int getPremiumSeats() { return premiumSeats; }
    public double getSlotPopularity() { return slotPopularity; }
}
