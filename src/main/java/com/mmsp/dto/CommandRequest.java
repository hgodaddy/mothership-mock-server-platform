package com.mmsp.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class CommandRequest {

    @NotBlank
    private String commandType;

    private String correlationId;
    private Map<String, Object> payload;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
