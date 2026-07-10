package com.festquant.domain;

import java.time.LocalDateTime;

public class Event {
    private final String eventId;
    private final String eventName;
    private final String category;
    private final String venueId;
    private final int capacity;
    private final double basePrice;
    private final double minPrice;
    private final double maxPrice;
    private final LocalDateTime eventDateTime;
    private final int premiumSeats;
    private final double slotPopularity;

    public Event(String eventId, String eventName, String category, String venueId, int capacity,
                 double basePrice, double minPrice, double maxPrice, LocalDateTime eventDateTime,
                 int premiumSeats, double slotPopularity) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.category = category;
        this.venueId = venueId;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.eventDateTime = eventDateTime;
        this.premiumSeats = premiumSeats;
        this.slotPopularity = slotPopularity;
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getCategory() { return category; }
    public String getVenueId() { return venueId; }
    public int getCapacity() { return capacity; }
    public double getBasePrice() { return basePrice; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }
    public int getPremiumSeats() { return premiumSeats; }
    public double getSlotPopularity() { return slotPopularity; }
}
