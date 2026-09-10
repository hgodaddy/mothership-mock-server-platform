package com.mmsp.controller;

import com.mmsp.agents.StabilityAgent;
import com.mmsp.dto.ScenarioSetRequest;
import com.mmsp.model.GlobalScenario;
import com.mmsp.service.ScenarioManager;
import com.mmsp.service.StateStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Scenarios")
public class ScenarioController {

    private final ScenarioManager scenarioManager;
    private final StateStore stateStore;
    private final StabilityAgent stabilityAgent;

    public ScenarioController(
            ScenarioManager scenarioManager,
            StateStore stateStore,
            StabilityAgent stabilityAgent
    ) {
        this.scenarioManager = scenarioManager;
        this.stateStore = stateStore;
        this.stabilityAgent = stabilityAgent;
    }

    @GetMapping("/scenarios")
    @Operation(summary = "List scenarios and active global scenario")
    public Map<String, Object> list() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("active", stateStore.getGlobalScenario());
        body.put("scenarios", scenarioManager.list());
        return body;
    }

    @PutMapping("/scenarios/active")
    @Operation(summary = "Set global active scenario")
    public Map<String, Object> setActive(@Valid @RequestBody ScenarioSetRequest request) {
        GlobalScenario active = scenarioManager.setGlobal(request.getScenario(), request.getLatencyMs());
        stabilityAgent.onScenarioChange(Map.of(
                "scope", "global",
                "scenario", active.getScenario(),
                "latencyMs", active.getLatencyMs(),
                "updatedAt", active.getUpdatedAt()
        ));
        return Map.of("active", active);
    }
}
