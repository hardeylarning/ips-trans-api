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

    @Value("${PS_BASE_URL}")
    private String PS_BASE_URL;

    @Value("${PS_TEST_KEY}")
    private String PS_API_KEY;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().defaultHeaders(httpHeaders -> {
//            httpHeaders.add("Authorization", "Bearer "+API_KEY);
            httpHeaders.setBearerAuth(API_KEY);
        }).baseUrl(BASE_URL).build();
    }

    @Bean
    public WebClient paystackClient() {
        return WebClient.builder().defaultHeaders(httpHeaders -> {
//            httpHeaders.add("Authorization", "Bearer "+API_KEY);
            httpHeaders.setBearerAuth(PS_API_KEY);
        }).baseUrl(PS_BASE_URL).build();
    }
}
