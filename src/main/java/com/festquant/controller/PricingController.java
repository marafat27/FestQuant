/**
 * Contains the pricing controller implementation used by FestQuant.
 */
package com.festquant.controller;

import com.festquant.domain.PriceRecommendation;
import com.festquant.pricing.PriceSimulationRow;
import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes HTTP endpoints for pricing operations.
 */
@RestController
@RequestMapping("/api/pricing")
public final class PricingController {
    // Stores the analytics used by this class.
    private final AnalyticsFacade analytics;

    /**
     * Creates a PricingController with the values needed by this component.
     */
    public PricingController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    /**
     * Recommends all prices.
     */
    @PostMapping("/recommend-all")
    public List<PriceRecommendation> recommendAllPrices() {
        return analytics.refreshRecommendations();
    }

    /**
     * Recommends ations.
     */
    @GetMapping("/recommendations")
    public List<PriceRecommendation> recommendations() {
        return analytics.recommendations();
    }

    /**
     * Handles the simulations step.
     */
    @GetMapping("/simulations/{eventId}")
    public List<PriceSimulationRow> simulations(@PathVariable("eventId") String eventId) {
        return analytics.simulations(eventId);
    }
}
