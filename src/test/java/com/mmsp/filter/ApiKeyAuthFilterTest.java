package com.mmsp.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmsp.config.MmspProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyAuthFilterTest {

    @Test
    void publicPathsAreExempt() {
        assertTrue(ApiKeyAuthFilter.isPublicPath("/health"));
        assertTrue(ApiKeyAuthFilter.isPublicPath("/ready"));
        assertTrue(ApiKeyAuthFilter.isPublicPath("/api/docs"));
        assertTrue(ApiKeyAuthFilter.isPublicPath("/api/openapi"));
    }

    @Test
    void disabledAuthAllowsProtectedRoutes() throws Exception {
        MmspProperties properties = new MmspProperties();
        properties.getAuth().setEnabled(false);
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/devices/register");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void enabledAuthRejectsMissingKey() throws Exception {
        MmspProperties properties = new MmspProperties();
        properties.getAuth().setEnabled(true);
        properties.getAuth().setApiKey("secret-poc-key");
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/devices/register");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("UNAUTHORIZED"));
    }

    @Test
    void enabledAuthAcceptsMatchingKey() throws Exception {
        MmspProperties properties = new MmspProperties();
        properties.getAuth().setEnabled(true);
        properties.getAuth().setApiKey("secret-poc-key");
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/devices/register");
        request.addHeader("X-API-Key", "secret-poc-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
    }
}
