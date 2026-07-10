package com.festquant.model;

public class DemandFeatures {
    private final double logViews;          // log(views + 1) reduces huge raw view effect.
    private final double wishlistRate;      // wishlists / views.
    private final double soldRatio;         // cumulative sold / capacity.
    private final double urgencyIndex;      // Higher when event is closer.
    private final double slotPopularity;    // Event timing popularity.
    private final double priceIndex;        // candidate price / base price.

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

    public double getLogViews() { return logViews; }
    public double getWishlistRate() { return wishlistRate; }
    public double getSoldRatio() { return soldRatio; }
    public double getUrgencyIndex() { return urgencyIndex; }
    public double getSlotPopularity() { return slotPopularity; }
    public double getPriceIndex() { return priceIndex; }
}
