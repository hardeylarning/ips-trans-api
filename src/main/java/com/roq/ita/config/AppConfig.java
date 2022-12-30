package com.roq.ita.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${FW_BASE_URL}")
    private String BASE_URL;

    @Value("${FW_TEST_KEY}")
    private String API_KEY;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().defaultHeaders(httpHeaders -> {
//            httpHeaders.add("Authorization", "Bearer "+API_KEY);
            httpHeaders.setBearerAuth(API_KEY);
        }).baseUrl(BASE_URL).build();
    }
}
