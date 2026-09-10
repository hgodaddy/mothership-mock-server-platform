package com.mmsp.model;

import java.util.Map;

public class ScenarioApplicationResult {
    private final boolean shortCircuit;
    private final int statusCode;
    private final Map<String, Object> body;
    private final ResolvedScenario resolved;

    public ScenarioApplicationResult(
            boolean shortCircuit,
            int statusCode,
            Map<String, Object> body,
            ResolvedScenario resolved
    ) {
        this.shortCircuit = shortCircuit;
        this.statusCode = statusCode;
        this.body = body;
        this.resolved = resolved;
    }

    public static ScenarioApplicationResult continueWith(ResolvedScenario resolved) {
        return new ScenarioApplicationResult(false, 0, null, resolved);
    }

    public static ScenarioApplicationResult shortCircuit(int statusCode, Map<String, Object> body, ResolvedScenario resolved) {
        return new ScenarioApplicationResult(true, statusCode, body, resolved);
    }

    public boolean isShortCircuit() {
        return shortCircuit;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public ResolvedScenario getResolved() {
        return resolved;
    }
}
