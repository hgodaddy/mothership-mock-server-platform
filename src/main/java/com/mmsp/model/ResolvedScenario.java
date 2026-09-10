package com.mmsp.model;

public class ResolvedScenario {
    private final String key;
    private final String source;
    private final ScenarioDefinition definition;
    private final int latencyMs;

    public ResolvedScenario(String key, String source, ScenarioDefinition definition, int latencyMs) {
        this.key = key;
        this.source = source;
        this.definition = definition;
        this.latencyMs = latencyMs;
    }

    public String getKey() {
        return key;
    }

    public String getSource() {
        return source;
    }

    public ScenarioDefinition getDefinition() {
        return definition;
    }

    public int getLatencyMs() {
        return latencyMs;
    }
}
