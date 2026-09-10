package com.mmsp.service;

import com.mmsp.config.MmspProperties;
import com.mmsp.model.ScenarioApplicationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioManagerTest {

    private ScenarioManager scenarioManager;

    @BeforeEach
    void setUp() {
        MmspProperties properties = new MmspProperties();
        properties.setScenariosFile("classpath:config/scenarios.yaml");
        properties.setDefaultScenario("success");
        properties.setDefaultLatencyMs(1);
        StateStore stateStore = new InMemoryStateStore();
        ScenarioCatalogLoader loader = new ScenarioCatalogLoader(properties, new DefaultResourceLoader());
        scenarioManager = new ScenarioManager(properties, stateStore, loader);
        scenarioManager.init();
    }

    @Test
    void listsFiveMvpScenarios() {
        List<String> keys = scenarioManager.list().stream()
                .map(item -> String.valueOf(item.get("key")))
                .sorted()
                .collect(Collectors.toList());
        assertEquals(
                List.of("device_offline", "internal_error", "success", "timeout", "validation_failure"),
                keys
        );
    }

    @Test
    void resolvesHeaderScenarioOverGlobal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-MMSP-Scenario", "timeout");
        assertEquals("timeout", scenarioManager.resolve(request, null).getKey());
        assertEquals("header", scenarioManager.resolve(request, null).getSource());
    }

    @Test
    void shortCircuitsInternalErrorScenario() {
        scenarioManager.setGlobal("internal_error", 1);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("correlationId", "t1");
        ScenarioApplicationResult result = scenarioManager.apply(request, null);
        assertTrue(result.isShortCircuit());
        assertEquals(500, result.getStatusCode());
        assertEquals("INTERNAL_ERROR", result.getBody().get("error"));
    }
}
