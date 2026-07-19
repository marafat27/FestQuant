/**
 * Contains the event implementation used by FestQuant.
 */
package com.festquant.domain;

import java.time.LocalDateTime;

/**
 * Represents the event part of the FestQuant application.
 */
public class Event {
    // Stores the event id used by this class.
    private final String eventId;
    // Stores the event name used by this class.
    private final String eventName;
    // Stores the category used by this class.
    private final String category;
    // Stores the venue id used by this class.
    private final String venueId;
    // Stores the capacity used by this class.
    private final int capacity;
    // Stores the base price used by this class.
    private final double basePrice;
    // Stores the min price used by this class.
    private final double minPrice;
    // Stores the max price used by this class.
    private final double maxPrice;
    // Stores the event date time used by this class.
    private final LocalDateTime eventDateTime;
    // Stores the premium seats used by this class.
    private final int premiumSeats;
    // Stores the slot popularity used by this class.
    private final double slotPopularity;

    /**
     * Creates a Event with the values needed by this component.
     */
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

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns event name.
     */
    public String getEventName() { return eventName; }
    /**
     * Returns category.
     */
    public String getCategory() { return category; }
    /**
     * Returns venue id.
     */
    public String getVenueId() { return venueId; }
    /**
     * Returns capacity.
     */
    public int getCapacity() { return capacity; }
    /**
     * Returns base price.
     */
    public double getBasePrice() { return basePrice; }
    /**
     * Returns min price.
     */
    public double getMinPrice() { return minPrice; }
    /**
     * Returns max price.
     */
    public double getMaxPrice() { return maxPrice; }
    /**
     * Returns event date time.
     */
    public LocalDateTime getEventDateTime() { return eventDateTime; }
    /**
     * Returns premium seats.
     */
    public int getPremiumSeats() { return premiumSeats; }
    /**
     * Returns slot popularity.
     */
    public double getSlotPopularity() { return slotPopularity; }
}
