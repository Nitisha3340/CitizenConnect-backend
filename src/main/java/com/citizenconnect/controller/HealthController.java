package com.citizenconnect.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for load balancers (Railway) and quick sanity checks in the browser.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "CitizenConnect API");
        body.put("status", "UP");
        body.put("health", "/health");
        body.put("swaggerUi", "/swagger-ui/index.html");
        body.put("openApi", "/v3/api-docs");
        return body;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
