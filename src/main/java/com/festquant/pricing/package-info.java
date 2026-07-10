/**
 * Dynamic pricing rules and price-simulation records.
 *
 * <p>The {@link com.festquant.pricing.PricingStrategy} abstraction lets the
 * project swap pricing policies without changing controllers or repositories.
 * The current strategy evaluates candidate prices, predicts demand, applies
 * capacity limits and enforces fairness caps before recommending a final price.</p>
 */
package com.festquant.pricing;
