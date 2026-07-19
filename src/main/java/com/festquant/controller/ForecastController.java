/**
 * Contains the forecast controller implementation used by FestQuant.
 */
package com.festquant.controller;

import com.festquant.model.ForecastResult;
import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes HTTP endpoints for forecast operations.
 */
@RestController
@RequestMapping("/api/forecast")
public final class ForecastController {
    // Stores the analytics used by this class.
    private final AnalyticsFacade analytics;

    /**
     * Creates a ForecastController with the values needed by this component.
     */
    public ForecastController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    /**
     * Handles the forecasts step.
     */
    @GetMapping
    public List<ForecastResult> forecasts() {
        return analytics.forecasts();
    }

    /**
     * Runs all.
     */
    @PostMapping("/run-all")
    public List<ForecastResult> runAll() {
        analytics.refreshRecommendations();
        return analytics.forecasts();
    }
}
