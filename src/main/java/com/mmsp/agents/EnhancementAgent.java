package com.mmsp.agents;

import com.mmsp.config.MmspProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EnhancementAgent {

    private static final Logger log = LoggerFactory.getLogger(EnhancementAgent.class);

    private final boolean enabled;

    public EnhancementAgent(MmspProperties properties) {
        this.enabled = properties.getAgents().isEnhancementEnabled();
    }

    public void onBoot(List<Map<String, Object>> catalog) {
        if (!enabled) {
            return;
        }
        List<String> keys = catalog.stream()
                .map(item -> String.valueOf(item.get("key")))
                .collect(Collectors.toList());
        log.info("EnhancementAgent signal: scenario_catalog_loaded mode=stub scenarioCount={} scenarios={}",
                catalog.size(), keys);
    }

    public void onApiInvocation(Map<String, Object> meta) {
        if (!enabled) {
            return;
        }
        log.debug("EnhancementAgent signal: api_invocation_observed mode=stub {}", meta);
    }
}
