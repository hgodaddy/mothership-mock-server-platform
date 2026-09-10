package com.mmsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "mmsp")
public class MmspProperties {

    private String platformName = "MMSP";
    private String platformVersion = "0.1.0";
    private String apiPrefix = "/api/v1";
    private String defaultScenario = "success";
    private int defaultLatencyMs = 50;
    private String scenariosFile = "classpath:config/scenarios.yaml";
    private boolean redisEnabled = false;
    private String redisUrl = "redis://localhost:6379";
    private boolean requestLogging = true;
    private boolean metricsEnabled = true;
    private List<String> deviceStates = new ArrayList<>(List.of(
            "online", "offline", "busy", "error", "maintenance"
    ));
    private Agents agents = new Agents();
    private Auth auth = new Auth();

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public String getDefaultScenario() {
        return defaultScenario;
    }

    public void setDefaultScenario(String defaultScenario) {
        this.defaultScenario = defaultScenario;
    }

    public int getDefaultLatencyMs() {
        return defaultLatencyMs;
    }

    public void setDefaultLatencyMs(int defaultLatencyMs) {
        this.defaultLatencyMs = defaultLatencyMs;
    }

    public String getScenariosFile() {
        return scenariosFile;
    }

    public void setScenariosFile(String scenariosFile) {
        this.scenariosFile = scenariosFile;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public void setRedisUrl(String redisUrl) {
        this.redisUrl = redisUrl;
    }

    public boolean isRequestLogging() {
        return requestLogging;
    }

    public void setRequestLogging(boolean requestLogging) {
        this.requestLogging = requestLogging;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public List<String> getDeviceStates() {
        return deviceStates;
    }

    public void setDeviceStates(List<String> deviceStates) {
        this.deviceStates = deviceStates;
    }

    public Agents getAgents() {
        return agents;
    }

    public void setAgents(Agents agents) {
        this.agents = agents;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public static class Auth {
        private boolean enabled = false;
        private String apiKey = "";
        private String headerName = "X-API-Key";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    public static class Agents {
        private boolean stabilityEnabled = true;
        private boolean enhancementEnabled = true;

        public boolean isStabilityEnabled() {
            return stabilityEnabled;
        }

        public void setStabilityEnabled(boolean stabilityEnabled) {
            this.stabilityEnabled = stabilityEnabled;
        }

        public boolean isEnhancementEnabled() {
            return enhancementEnabled;
        }

        public void setEnhancementEnabled(boolean enhancementEnabled) {
            this.enhancementEnabled = enhancementEnabled;
        }
    }
}
