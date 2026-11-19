package com.ecoquest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Simple unauthenticated health check so Phase 1 setup can be verified
 * before any real feature (auth, entities, etc.) exists.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "ecoquest-india-backend",
                "timestamp", Instant.now().toString()
        );
    }
}
