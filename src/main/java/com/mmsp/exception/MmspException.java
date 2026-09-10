package com.mmsp.exception;

import java.util.List;
import java.util.Map;

public class MmspException extends RuntimeException {
    private final int statusCode;
    private final String code;
    private final List<Map<String, String>> details;

    public MmspException(int statusCode, String code, String message) {
        this(statusCode, code, message, null);
    }

    public MmspException(int statusCode, String code, String message, List<Map<String, String>> details) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.details = details;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    public List<Map<String, String>> getDetails() {
        return details;
    }
}
