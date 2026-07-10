package com.festquant.controller;

import com.festquant.domain.Event;
import com.festquant.service.AnalyticsFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public final class EventController {
    private final AnalyticsFacade analytics;

    public EventController(AnalyticsFacade analytics) {
        this.analytics = analytics;
    }

    @GetMapping
    public List<Event> events() {
        return analytics.events();
    }
}
