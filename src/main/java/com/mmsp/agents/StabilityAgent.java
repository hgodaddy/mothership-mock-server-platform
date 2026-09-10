package com.mmsp.agents;

import com.mmsp.config.MmspProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StabilityAgent {

    private static final Logger log = LoggerFactory.getLogger(StabilityAgent.class);

    private final boolean enabled;

    public StabilityAgent(MmspProperties properties) {
        this.enabled = properties.getAgents().isStabilityEnabled();
    }

    public void onBoot(Map<String, Object> stats) {
        if (!enabled) {
            return;
        }
        log.info("StabilityAgent signal: boot_health_watch mode=stub stats={}", stats);
    }

    public void onAnomaly(Map<String, Object> signal) {
        if (!enabled) {
            return;
        }
        log.warn("StabilityAgent signal: anomaly mode=stub {}", signal);
    }

    public void onScenarioChange(Map<String, Object> change) {
        if (!enabled) {
            return;
        }
        log.info("StabilityAgent signal: scenario_change_observed mode=stub {}", change);
    }

    public void onError(Map<String, Object> errorMeta) {
        if (!enabled) {
            return;
        }
        log.warn("StabilityAgent signal: error_budget_tick mode=stub {}", errorMeta);
    }
}
