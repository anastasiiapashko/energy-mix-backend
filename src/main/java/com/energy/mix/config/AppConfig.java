package com.energy.mix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration  // ← Mówi Spring: "ta klasa ma konfigurację"
public class AppConfig {
    
    @Bean  // ← Mówi Spring: "stwórz obiekt RestTemplate i daj go do kontenera"
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}