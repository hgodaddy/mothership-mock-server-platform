package com.mmsp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI mmspOpenApi(MmspProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title("Mothership Mock Server Platform (MMSP) API")
                        .description("Sprint 1 Java POC APIs for Mission Control device simulation. "
                                + "Use header `X-MMSP-Scenario` to override the active scenario per request.")
                        .version(properties.getPlatformVersion())
                        .contact(new Contact()
                                .name("POS QA Engineering")
                                .email("hemant.srivastava@example.com")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local POC")));
    }
}
