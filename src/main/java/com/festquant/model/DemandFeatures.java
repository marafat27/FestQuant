/**
 * Contains the demand features implementation used by FestQuant.
 */
package com.festquant.model;

/**
 * Represents the demand features part of the FestQuant application.
 */
public class DemandFeatures {
    private final double logViews;          // log(views + 1) reduces huge raw view effect.
    // Stores the wishlist rate used by this class.
    private final double wishlistRate;      // wishlists / views.
    // Stores the sold ratio used by this class.
    private final double soldRatio;         // cumulative sold / capacity.
    // Stores the urgency index used by this class.
    private final double urgencyIndex;      // Higher when event is closer.
    // Stores the slot popularity used by this class.
    private final double slotPopularity;    // Event timing popularity.
    // Stores the price index used by this class.
    private final double priceIndex;        // candidate price / base price.

    /**
     * Creates a DemandFeatures with the values needed by this component.
     */
    public DemandFeatures(
            double logViews,
            double wishlistRate,
            double soldRatio,
            double urgencyIndex,
            double slotPopularity,
            double priceIndex
    ) {
        this.logViews = logViews;
        this.wishlistRate = wishlistRate;
        this.soldRatio = soldRatio;
        this.urgencyIndex = urgencyIndex;
        this.slotPopularity = slotPopularity;
        this.priceIndex = priceIndex;
    }

    /**
     * Returns log views.
     */
    public double getLogViews() { return logViews; }
    /**
     * Returns wishlist rate.
     */
    public double getWishlistRate() { return wishlistRate; }
    /**
     * Returns sold ratio.
     */
    public double getSoldRatio() { return soldRatio; }
    /**
     * Returns urgency index.
     */
    public double getUrgencyIndex() { return urgencyIndex; }
    /**
     * Returns slot popularity.
     */
    public double getSlotPopularity() { return slotPopularity; }
    /**
     * Returns price index.
     */
    public double getPriceIndex() { return priceIndex; }
}
