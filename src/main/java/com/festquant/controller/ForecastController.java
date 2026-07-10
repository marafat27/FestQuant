package com.festquant.controller;

import com.festquant.model.ForecastResult;
import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forecast")
public final class ForecastController {
    private final AnalyticsFacade analytics;

    public ForecastController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @GetMapping
    public List<ForecastResult> forecasts() {
        return analytics.forecasts();
    }

    @PostMapping("/run-all")
    public List<ForecastResult> runAll() {
        analytics.refreshRecommendations();
        return analytics.forecasts();
    }
}
