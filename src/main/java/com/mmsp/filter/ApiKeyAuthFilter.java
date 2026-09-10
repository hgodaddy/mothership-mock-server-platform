package com.mmsp.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmsp.config.MmspProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final MmspProperties properties;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(MmspProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.getAuth().isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        return isPublicPath(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String expected = properties.getAuth().getApiKey();
        String provided = request.getHeader(properties.getAuth().getHeaderName());
        if (expected == null || expected.isBlank()) {
            writeUnauthorized(request, response, "API key authentication is enabled but MMSP_API_KEY is not configured");
            return;
        }
        if (provided == null || !secureEquals(expected, provided)) {
            writeUnauthorized(request, response, "Valid " + properties.getAuth().getHeaderName() + " header is required");
            return;
        }
        filterChain.doFilter(request, response);
    }

    static boolean isPublicPath(String path) {
        if (path == null) {
            return false;
        }
        return path.equals("/")
                || path.equals("/health")
                || path.equals("/ready")
                || path.equals("/metrics")
                || path.startsWith("/api/docs")
                || path.startsWith("/api/openapi")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator/health");
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "UNAUTHORIZED");
        body.put("message", message);
        body.put("correlationId", request.getAttribute("correlationId"));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private static boolean secureEquals(String expected, String provided) {
        byte[] left = expected.getBytes(StandardCharsets.UTF_8);
        byte[] right = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }
}
