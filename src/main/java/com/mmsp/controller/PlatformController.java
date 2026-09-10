package com.mmsp.controller;

import com.mmsp.config.MmspProperties;
import com.mmsp.service.StateStore;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Platform")
public class PlatformController {

    private final MmspProperties properties;
    private final StateStore stateStore;
    private final PrometheusMeterRegistry prometheusMeterRegistry;
    private final Instant startedAt = Instant.now();

    public PlatformController(
            MmspProperties properties,
            StateStore stateStore,
            PrometheusMeterRegistry prometheusMeterRegistry
    ) {
        this.properties = properties;
        this.stateStore = stateStore;
        this.prometheusMeterRegistry = prometheusMeterRegistry;
    }

    @GetMapping("/")
    @Operation(summary = "Platform root")
    public Map<String, Object> root() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", properties.getPlatformName());
        body.put("version", properties.getPlatformVersion());
        body.put("description", "Mothership Mock Server Platform Sprint 1 Java POC");
        body.put("docs", "/api/docs");
        body.put("health", "/health");
        body.put("ready", "/ready");
        body.put("metrics", "/metrics");
        body.put("apiPrefix", properties.getApiPrefix());
        return body;
    }

    @GetMapping("/health")
    @Operation(summary = "Liveness probe")
    public Map<String, Object> health() {
        double uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("service", properties.getPlatformName());
        body.put("version", properties.getPlatformVersion());
        body.put("uptimeSeconds", Math.round(uptimeSeconds * 10.0) / 10.0);
        body.put("startedAt", startedAt.toString());
        return body;
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness probe")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> stats = stateStore.stats();
        boolean scenarioReady = stats.get("globalScenario") != null;
        boolean storeReady = stateStore.healthy();
        boolean ready = scenarioReady && storeReady;
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("scenarioManager", scenarioReady);
        checks.put("stateStore", storeReady);
        checks.put("stateBackend", stateStore.backend());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ready ? "ready" : "not_ready");
        body.put("checks", checks);
        body.put("stats", stats);
        return ResponseEntity.status(ready ? 200 : 503).body(body);
    }

    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Prometheus metrics")
    public String metrics() {
        return prometheusMeterRegistry.scrape();
    }
}
