package com.mmsp.dto;

import jakarta.validation.constraints.NotBlank;

public class ScenarioSetRequest {

    @NotBlank
    private String scenario;

    private Integer latencyMs;

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }
}
