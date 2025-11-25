package com.energy.mix.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration  // This class provides configuration for Spring
public class AppConfig {
    
    @Bean  // Creates a RestTemplate for making HTTP requests to external APIs
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean  // Creates an ObjectMapper for converting JSON to Java objects and back
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}