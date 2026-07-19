/**
 * Contains the pricing event implementation used by FestQuant.
 */
package com.festquant.domain;

/**
 * Represents the pricing event part of the FestQuant application.
 */
public class PricingEvent {
    // Stores the event id used by this class.
    private final String eventId;              // Unique event ID.
    // Stores the event name used by this class.
    private final String eventName;            // Event name for reports.
    // Stores the event datetime used by this class.
    private final String eventDatetime;        // Event date-time string.
    // Stores the base price used by this class.
    private final double basePrice;            // Original fixed price.
    // Stores the min price used by this class.
    private final double minPrice;             // Lower allowed price.
    // Stores the max price used by this class.
    private final double maxPrice;             // Upper allowed price.
    // Stores the capacity used by this class.
    private final int capacity;                // Total seats.
    // Stores the premium seats used by this class.
    private final int premiumSeats;            // Seats reserved for auction.
    // Stores the slot popularity used by this class.
    private final double slotPopularity;       // Timing popularity score.

    /**
     * Creates a PricingEvent with the values needed by this component.
     */
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

    /**
     * Returns event id.
     */
    public String getEventId() { return eventId; }
    /**
     * Returns event name.
     */
    public String getEventName() { return eventName; }
    /**
     * Returns event datetime.
     */
    public String getEventDatetime() { return eventDatetime; }
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
     * Returns capacity.
     */
    public int getCapacity() { return capacity; }
    /**
     * Returns premium seats.
     */
    public int getPremiumSeats() { return premiumSeats; }
    /**
     * Returns slot popularity.
     */
    public double getSlotPopularity() { return slotPopularity; }
}
