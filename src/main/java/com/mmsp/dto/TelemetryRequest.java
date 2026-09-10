package com.mmsp.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class TelemetryRequest {

    @NotBlank
    private String metricType;

    private String recordedAt;
    private Map<String, Object> values;

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
}
