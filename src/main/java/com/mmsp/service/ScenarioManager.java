package com.mmsp.service;

import com.mmsp.config.MmspProperties;
import com.mmsp.exception.MmspException;
import com.mmsp.model.Device;
import com.mmsp.model.GlobalScenario;
import com.mmsp.model.ResolvedScenario;
import com.mmsp.model.ScenarioApplicationResult;
import com.mmsp.model.ScenarioDefinition;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioManager {

    private static final Logger log = LoggerFactory.getLogger(ScenarioManager.class);

    private final MmspProperties properties;
    private final StateStore stateStore;
    private final ScenarioCatalogLoader catalogLoader;
    private Map<String, ScenarioDefinition> catalog = new LinkedHashMap<>();

    public ScenarioManager(MmspProperties properties, StateStore stateStore, ScenarioCatalogLoader catalogLoader) {
        this.properties = properties;
        this.stateStore = stateStore;
        this.catalogLoader = catalogLoader;
    }

    @PostConstruct
    public void init() {
        this.catalog = catalogLoader.load();
        stateStore.setGlobalScenario(properties.getDefaultScenario(), properties.getDefaultLatencyMs());
        log.info("Loaded {} scenarios; default={}", catalog.size(), properties.getDefaultScenario());
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (ScenarioDefinition def : catalog.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", def.getKey());
            item.put("description", def.getDescription());
            item.put("httpStatusOverride", def.getHttpStatusOverride());
            item.put("latencyMs", def.getLatencyMs());
            items.add(item);
        }
        return items;
    }

    public boolean exists(String key) {
        return catalog.containsKey(key);
    }

    public ScenarioDefinition getDefinition(String key) {
        return catalog.get(key);
    }

    public GlobalScenario setGlobal(String scenario, Integer latencyMs) {
        requireKnown(scenario);
        ScenarioDefinition def = getDefinition(scenario);
        int appliedLatency = latencyMs != null
                ? latencyMs
                : (def.getLatencyMs() != null ? def.getLatencyMs() : properties.getDefaultLatencyMs());
        GlobalScenario active = stateStore.setGlobalScenario(scenario, appliedLatency);
        log.info("Global scenario updated: {}", active.getScenario());
        return active;
    }

    public Device setDeviceScenario(String deviceId, String scenario, Integer latencyMs) {
        requireKnown(scenario);
        Device device = stateStore.getDevice(deviceId)
                .orElseThrow(() -> new MmspException(404, "DEVICE_NOT_FOUND", "Device not found: " + deviceId));
        ScenarioDefinition def = getDefinition(scenario);
        int appliedLatency = latencyMs != null
                ? latencyMs
                : (def.getLatencyMs() != null ? def.getLatencyMs() : properties.getDefaultLatencyMs());
        return stateStore.updateDevice(deviceId, d -> {
            d.setScenario(scenario);
            d.setScenarioLatencyMs(appliedLatency);
        }).orElse(device);
    }

    public ResolvedScenario resolve(HttpServletRequest request, Device device) {
        String headerScenario = request.getHeader("X-MMSP-Scenario");
        if (headerScenario != null && !headerScenario.isBlank()) {
            requireKnown(headerScenario);
            ScenarioDefinition def = getDefinition(headerScenario);
            return new ResolvedScenario(headerScenario, "header", def, valueOrDefault(def.getLatencyMs()));
        }

        if (device != null && device.getScenario() != null && !device.getScenario().isBlank()) {
            ScenarioDefinition def = getDefinition(device.getScenario());
            int latency = device.getScenarioLatencyMs() != null
                    ? device.getScenarioLatencyMs()
                    : valueOrDefault(def.getLatencyMs());
            return new ResolvedScenario(device.getScenario(), "device", def, latency);
        }

        GlobalScenario global = stateStore.getGlobalScenario();
        ScenarioDefinition def = getDefinition(global.getScenario());
        return new ResolvedScenario(global.getScenario(), "global", def, global.getLatencyMs());
    }

    public ScenarioApplicationResult apply(HttpServletRequest request, Device device) {
        ResolvedScenario resolved = resolve(request, device);
        ScenarioDefinition def = resolved.getDefinition();
        sleep(resolved.getLatencyMs());

        if (device != null && def.getForceDeviceState() != null) {
            stateStore.updateDevice(device.getDeviceId(), d -> d.setState(def.getForceDeviceState()));
        }

        if (def.getHttpStatusOverride() != null) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", def.getErrorCode());
            body.put("message", def.getErrorMessage());
            body.put("scenario", resolved.getKey());
            body.put("scenarioSource", resolved.getSource());
            body.put("latencyMs", resolved.getLatencyMs());
            if (device != null) {
                body.put("deviceId", device.getDeviceId());
            }
            if ("validation_failure".equals(resolved.getKey())) {
                body.put("details", List.of(Map.of(
                        "field", "payload",
                        "issue", "Simulated invalid field for negative testing"
                )));
            }
            Object correlationId = request.getAttribute("correlationId");
            if (correlationId != null) {
                body.put("correlationId", correlationId);
            }
            return ScenarioApplicationResult.shortCircuit(def.getHttpStatusOverride(), body, resolved);
        }

        return ScenarioApplicationResult.continueWith(resolved);
    }

    private void requireKnown(String scenario) {
        if (!exists(scenario)) {
            throw new MmspException(400, "UNKNOWN_SCENARIO", "Unknown scenario: " + scenario);
        }
    }

    private int valueOrDefault(Integer latencyMs) {
        return latencyMs != null ? latencyMs : properties.getDefaultLatencyMs();
    }

    private void sleep(int latencyMs) {
        if (latencyMs <= 0) {
            return;
        }
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MmspException(500, "INTERNAL_ERROR", "Interrupted while applying scenario latency");
        }
    }
}
