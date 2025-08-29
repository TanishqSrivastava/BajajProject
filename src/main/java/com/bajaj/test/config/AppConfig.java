package com.bajaj.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application configuration class.
 * Defines beans that will be managed by the Spring container.
 */
@Configuration
public class AppConfig {



    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
