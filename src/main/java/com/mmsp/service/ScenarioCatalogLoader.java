package com.mmsp.service;

import com.mmsp.config.MmspProperties;
import com.mmsp.model.ScenarioDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ScenarioCatalogLoader {

    private final MmspProperties properties;
    private final ResourceLoader resourceLoader;

    public ScenarioCatalogLoader(MmspProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @SuppressWarnings("unchecked")
    public Map<String, ScenarioDefinition> load() {
        try {
            Resource resource = resourceLoader.getResource(properties.getScenariosFile());
            try (InputStream in = resource.getInputStream()) {
                Yaml yaml = new Yaml();
                Map<String, Object> root = yaml.load(in);
                Map<String, Object> scenarios = (Map<String, Object>) root.getOrDefault("scenarios", Collections.emptyMap());
                Map<String, ScenarioDefinition> catalog = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : scenarios.entrySet()) {
                    Map<String, Object> raw = (Map<String, Object>) entry.getValue();
                    ScenarioDefinition def = new ScenarioDefinition();
                    def.setKey(asString(raw.getOrDefault("key", entry.getKey())));
                    def.setDescription(asString(raw.get("description")));
                    def.setHttpStatusOverride(asInteger(raw.get("httpStatusOverride")));
                    def.setLatencyMs(asInteger(raw.get("latencyMs")));
                    def.setErrorCode(asString(raw.get("errorCode")));
                    def.setErrorMessage(asString(raw.get("errorMessage")));
                    def.setForceDeviceState(asString(raw.get("forceDeviceState")));
                    catalog.put(def.getKey(), def);
                }
                return catalog;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load scenarios from " + properties.getScenariosFile(), ex);
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value);
        if ("null".equalsIgnoreCase(text) || text.isBlank()) {
            return null;
        }
        return Integer.parseInt(text);
    }
}
