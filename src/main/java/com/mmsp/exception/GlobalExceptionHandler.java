package com.mmsp.exception;

import com.mmsp.agents.StabilityAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final StabilityAgent stabilityAgent;

    public GlobalExceptionHandler(StabilityAgent stabilityAgent) {
        this.stabilityAgent = stabilityAgent;
    }

    @ExceptionHandler(MmspException.class)
    public ResponseEntity<Map<String, Object>> handleMmsp(MmspException ex, HttpServletRequest request) {
        log.error("Request failed path={} code={} message={}",
                request.getRequestURI(), ex.getCode(), ex.getMessage());
        stabilityAgent.onError(Map.of(
                "path", request.getRequestURI(),
                "statusCode", ex.getStatusCode(),
                "code", ex.getCode(),
                "correlationId", String.valueOf(request.getAttribute("correlationId"))
        ));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getCode());
        body.put("message", ex.getMessage());
        body.put("correlationId", request.getAttribute("correlationId"));
        if (ex.getDetails() != null) {
            body.put("details", ex.getDetails());
        }
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "VALIDATION_FAILURE");
        body.put("message", "Missing or invalid required field(s)");
        body.put("details", details);
        body.put("correlationId", request.getAttribute("correlationId"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error path={}", request.getRequestURI(), ex);
        stabilityAgent.onError(Map.of(
                "path", request.getRequestURI(),
                "statusCode", 500,
                "code", "INTERNAL_ERROR",
                "correlationId", String.valueOf(request.getAttribute("correlationId"))
        ));
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INTERNAL_ERROR");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error");
        body.put("correlationId", request.getAttribute("correlationId"));
        return ResponseEntity.internalServerError().body(body);
    }

    private Map<String, String> toDetail(FieldError error) {
        Map<String, String> detail = new LinkedHashMap<>();
        detail.put("field", error.getField());
        detail.put("issue", error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid");
        return detail;
    }
}
