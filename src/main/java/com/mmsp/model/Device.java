package com.mmsp.model;

import java.util.HashMap;
import java.util.Map;

public class Device {
    private String deviceId;
    private String deviceSerial;
    private String deviceType;
    private String firmwareVersion;
    private String siteId;
    private Map<String, Object> metadata = new HashMap<>();
    private String status;
    private String state;
    private String scenario;
    private Integer scenarioLatencyMs;
    private String lastHeartbeatAt;
    private Map<String, Object> lastHeartbeatMetrics;
    private String statusReason;
    private String lastCommandId;
    private String registeredAt;
    private String updatedAt;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Integer getScenarioLatencyMs() {
        return scenarioLatencyMs;
    }

    public void setScenarioLatencyMs(Integer scenarioLatencyMs) {
        this.scenarioLatencyMs = scenarioLatencyMs;
    }

    public String getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(String lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }

    public Map<String, Object> getLastHeartbeatMetrics() {
        return lastHeartbeatMetrics;
    }

    public void setLastHeartbeatMetrics(Map<String, Object> lastHeartbeatMetrics) {
        this.lastHeartbeatMetrics = lastHeartbeatMetrics;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public String getLastCommandId() {
        return lastCommandId;
    }

    public void setLastCommandId(String lastCommandId) {
        this.lastCommandId = lastCommandId;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
