package com.mmsp;

import com.mmsp.agents.EnhancementAgent;
import com.mmsp.agents.StabilityAgent;
import com.mmsp.config.MmspProperties;
import com.mmsp.service.ScenarioManager;
import com.mmsp.service.StateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
@EnableConfigurationProperties(MmspProperties.class)
public class MmspApplication {

    private static final Logger log = LoggerFactory.getLogger(MmspApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MmspApplication.class, args);
    }

    @Bean
    ApplicationRunner bootSignals(
            MmspProperties properties,
            StateStore stateStore,
            ScenarioManager scenarioManager,
            StabilityAgent stabilityAgent,
            EnhancementAgent enhancementAgent
    ) {
        return args -> {
            log.info("{} listening on port {} (apiPrefix={}, stateBackend={}, apiKeyAuth={})",
                    properties.getPlatformName(),
                    System.getenv().getOrDefault("PORT", "8080"),
                    properties.getApiPrefix(),
                    stateStore.backend(),
                    properties.getAuth().isEnabled());
            log.info("Swagger UI: http://localhost:{}/api/docs",
                    System.getenv().getOrDefault("PORT", "8080"));
            if (properties.getAuth().isEnabled()
                    && (properties.getAuth().getApiKey() == null || properties.getAuth().getApiKey().isBlank())) {
                log.warn("API key auth is enabled but MMSP_API_KEY is empty; protected routes will return 401");
            }
            stabilityAgent.onBoot(stateStore.stats());
            enhancementAgent.onBoot(scenarioManager.list());
        };
    }
}
