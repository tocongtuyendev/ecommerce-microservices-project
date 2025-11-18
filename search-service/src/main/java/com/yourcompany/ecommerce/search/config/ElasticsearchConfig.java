package com.yourcompany.ecommerce.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ElasticsearchConfig {


    @Value("${app.elasticsearch.url}")
    private String elasticsearchUrl;


    @Bean
    public WebClient elasticsearchWebClient() {
        return WebClient.builder()
                .baseUrl(elasticsearchUrl)
                .build();
    }
}