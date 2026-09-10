package com.mmsp.model;

public class GlobalScenario {
    private String scenario;
    private int latencyMs;
    private String updatedAt;

    public GlobalScenario() {
    }

    public GlobalScenario(String scenario, int latencyMs, String updatedAt) {
        this.scenario = scenario;
        this.latencyMs = latencyMs;
        this.updatedAt = updatedAt;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
