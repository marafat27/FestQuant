/**
 * Contains the event controller implementation used by FestQuant.
 */
package com.festquant.controller;

import com.festquant.domain.Event;
import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes HTTP endpoints for event operations.
 */
@RestController
@RequestMapping("/api/events")
public final class EventController {
    // Stores the analytics used by this class.
    private final AnalyticsFacade analytics;

    /**
     * Creates a EventController with the values needed by this component.
     */
    public EventController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    /**
     * Handles the events step.
     */
    @GetMapping
    public List<Event> events() {
        return analytics.events();
    }
}
