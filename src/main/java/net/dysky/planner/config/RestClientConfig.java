package net.dysky.planner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfig {

    @Bean
    public RestClient overpassRestClient(@Value("${overpass.api.url:https://overpass-api.de/api/interpreter}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public RestClient archiveRestClient(@Value("${weather.api.url:https://archive-api.open-meteo.com/v1/archive}")  String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public RestClient weatherRestClient(@Value("${weather.api.url:https://api.open-meteo.com/v1/forecast}")  String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public RestClient geocodingRestClient(@Value("${weather.geocoding.api.url:https://geocoding-api.open-meteo.com/v1/search}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

}
