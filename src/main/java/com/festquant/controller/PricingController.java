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

@RestController
@RequestMapping("/api/pricing")
public final class PricingController {
    private final AnalyticsFacade analytics;

    public PricingController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @PostMapping("/recommend-all")
    public List<PriceRecommendation> recommendAllPrices() {
        return analytics.refreshRecommendations();
    }

    @GetMapping("/recommendations")
    public List<PriceRecommendation> recommendations() {
        return analytics.recommendations();
    }

    @GetMapping("/simulations/{eventId}")
    public List<PriceSimulationRow> simulations(@PathVariable("eventId") String eventId) {
        return analytics.simulations(eventId);
    }
}
