/**
 * Contains the java nonlinear model adapter implementation used by FestQuant.
 */
package com.festquant.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java-side adapter for the nonlinear regression model trained in the
 * regression pipeline. The modelling step stores coefficients as JSON; this class
 * loads those coefficients and exposes a small prediction API that the Java
 * pricing strategy can use without depending on Python at runtime.
 */
public class JavaNonlinearModelAdapter {
    // Stores the beta0 used by this class.
    private double beta0 = -2.0;               // Safe fallback intercept.
    // Stores the beta views used by this class.
    private double betaViews = 0.45;           // View interest effect.
    // Stores the beta wishlist used by this class.
    private double betaWishlist = 1.20;        // Wishlist-conversion effect.
    // Stores the beta sold used by this class.
    private double betaSold = 1.00;            // Existing sold-ratio effect.
    // Stores the beta urgency used by this class.
    private double betaUrgency = 0.80;         // Higher urgency near event time.
    // Stores the beta slot used by this class.
    private double betaSlot = 0.70;            // Popular time-slot/category effect.
    // Stores the beta price used by this class.
    private double betaPrice = 1.10;           // Price penalty effect.

    /**
     * Reads the trained model file. The method accepts both compact Java-style
     * keys and the longer names emitted by the Python regression script, which
     * keeps the hand-off between the two modules stable.
     */
    public void loadFromPythonModel(Path modelPath) {
        if (!Files.exists(modelPath)) {
            System.out.println("Model JSON not found. Java fallback coefficients will be used: " + modelPath);
            return;
        }

        try {
            // Holds the json for this calculation.
            String json = Files.readString(modelPath);

            beta0 = extractAny(json, beta0, "beta0Intercept", "beta0", "intercept");
            betaViews = extractAny(json, betaViews, "beta1LogViews", "betaViews", "views", "logViews");
            betaWishlist = extractAny(json, betaWishlist, "beta2WishlistRate", "betaWishlistRate", "wishlistRate", "wishlist");
            betaSold = extractAny(json, betaSold, "beta3SoldRatio", "betaSoldRatio", "soldRatio");
            betaUrgency = extractAny(json, betaUrgency, "beta4UrgencyIndex", "betaUrgency", "urgencyIndex");
            betaSlot = extractAny(json, betaSlot, "beta5SlotPopularity", "betaSlotPopularity", "slotPopularity");
            betaPrice = extractAny(json, betaPrice, "beta6PriceSensitivity", "betaPriceIndex", "priceIndex", "price");
        } catch (IOException ex) {
            throw new RuntimeException("Could not read Python nonlinear model JSON.", ex);
        }
    }

    /**
     * Converts event demand features into a bounded demand ratio with a logistic
     * link function. A larger result means stronger expected demand at the
     * candidate price.
     */
    public double predictDemandRatio(DemandFeatures features) {
        // Regression score:
        // z = beta0 + beta1*logViews + beta2*wishlistRate + beta3*soldRatio
        //     + beta4*urgency + beta5*slotPopularity - beta6*priceIndex.
        double z =
                beta0
                + betaViews * features.getLogViews()
                + betaWishlist * features.getWishlistRate()
                + betaSold * features.getSoldRatio()
                + betaUrgency * features.getUrgencyIndex()
                + betaSlot * features.getSlotPopularity()
                - betaPrice * features.getPriceIndex();       // Higher price reduces demand.

        return sigmoid(z);
    }

    /**
     * Converts the predicted demand share into tickets: tickets = capacity * demand ratio.
     */
    public double predictTickets(double capacity, DemandFeatures features) {
        return capacity * predictDemandRatio(features);
    }

    /**
     * Applies the logistic link: demand ratio = 1 / (1 + exp(-z)).
     */
    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    /**
     * Reads the first matching numeric coefficient from the model JSON.
     */
    private double extractAny(String json, double defaultValue, String... keys) {
        // Uses key for the current item in the loop.
        for (String key : keys) {
            // Holds the matcher for this calculation.
            Matcher matcher = Pattern
                    .compile("\"" + key + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)")
                    .matcher(json);

            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        }

        return defaultValue;
    }
}
