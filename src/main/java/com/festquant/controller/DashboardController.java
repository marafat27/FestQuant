/**
 * Contains the dashboard controller implementation used by FestQuant.
 */
package com.festquant.controller;

import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes HTTP endpoints for dashboard operations.
 */
@RestController
@RequestMapping("/api/dashboard")
public final class DashboardController {
    // Stores the analytics used by this class.
    private final AnalyticsFacade analytics;

    /**
     * Creates a DashboardController with the values needed by this component.
     */
    public DashboardController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return analytics.dashboardSummary();
    }
}
