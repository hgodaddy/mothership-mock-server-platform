package com.mmsp.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceStatusRequest {

    @NotBlank
    private String state;

    private String reason;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
