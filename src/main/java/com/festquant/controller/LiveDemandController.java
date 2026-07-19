/**
 * Contains the live demand controller implementation used by FestQuant.
 */
package com.festquant.controller;

import com.festquant.service.LiveDemandService;
import com.festquant.stream.DemandScenario;
import com.festquant.stream.LiveDemandSnapshot;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Exposes HTTP endpoints for live demand operations.
 */
@RestController
@RequestMapping("/api/live")
public final class LiveDemandController {
    // Stores the live demand service used by this class.
    private final LiveDemandService liveDemandService;

    /**
     * Creates a LiveDemandController with the values needed by this component.
     */
    public LiveDemandController(LiveDemandService liveDemandService) {
        this.liveDemandService = liveDemandService;
    }

    /**
     * Handles the snapshots step.
     */
    @GetMapping("/snapshots")
    public List<LiveDemandSnapshot> snapshots() {
        return liveDemandService.snapshots();
    }

    /**
     * Handles the stream step.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return liveDemandService.subscribe();
    }

    @PostMapping("/start")
    public Map<String, Object> start() {
        return liveDemandService.start();
    }

    @PostMapping("/pause")
    public Map<String, Object> pause() {
        return liveDemandService.pause();
    }

    @PostMapping("/scenario/{scenario}")
    public Map<String, Object> scenario(@PathVariable("scenario") DemandScenario scenario) {
        return liveDemandService.useScenario(scenario);
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return liveDemandService.status();
    }
}
