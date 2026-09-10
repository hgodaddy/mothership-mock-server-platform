package com.mmsp.model;

public class ScenarioDefinition {
    private String key;
    private String description;
    private Integer httpStatusOverride;
    private Integer latencyMs;
    private String errorCode;
    private String errorMessage;
    private String forceDeviceState;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHttpStatusOverride() {
        return httpStatusOverride;
    }

    public void setHttpStatusOverride(Integer httpStatusOverride) {
        this.httpStatusOverride = httpStatusOverride;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getForceDeviceState() {
        return forceDeviceState;
    }

    public void setForceDeviceState(String forceDeviceState) {
        this.forceDeviceState = forceDeviceState;
    }
}
