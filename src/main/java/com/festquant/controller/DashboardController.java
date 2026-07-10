package com.festquant.controller;

import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public final class DashboardController {
    private final AnalyticsFacade analytics;

    public DashboardController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return analytics.dashboardSummary();
    }
}
