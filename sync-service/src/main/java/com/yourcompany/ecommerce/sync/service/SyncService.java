package com.yourcompany.ecommerce.sync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.Duration;


@Service
public class SyncService {


    private final WebClient searchClient;
    private final IdempotencyService idempotency;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String indexEndpoint;


    public SyncService(WebClient searchServiceWebClient, IdempotencyService idempotency, @Value("${app.search-service.indexEndpoint}") String indexEndpoint) {
        this.searchClient = searchServiceWebClient;
        this.idempotency = idempotency;
        this.indexEndpoint = indexEndpoint;
    }


    /**
     * Forward event to Search-service (indexing). Retries on transient HTTP errors.
     */
    @Retryable(value = { Exception.class }, maxAttempts = 4, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void handleEvent(String eventJson) throws Exception {
        JsonNode event = mapper.readTree(eventJson);
        String eventId = event.path("eventId").asText(null);
        String type = event.path("type").asText("unknown");
        JsonNode payload = event.path("payload");


        if (eventId == null || eventId.isEmpty()) {
// assign synthetic id (not recommended for production)
            eventId = "synthetic-" + System.currentTimeMillis();
        }


// Idempotency check
        if (idempotency.isProcessed(eventId)) {
            return; // already processed
        }


// Build request to Search-service
        String url = indexEndpoint; // e.g. /api/internal/index/product


// create body with event type + payload
        JsonNode body = mapper.createObjectNode()
                .put("eventId", eventId)
                .put("type", type)
                .set("payload", payload);


// call search-service (reactive) but block with timeout to keep listener simple
        Mono<String> resp = searchClient.post()
                .uri(url)
                .bodyValue(mapper.writeValueAsString(body))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10));


        String result = resp.block(Duration.ofSeconds(12));


// mark processed on success
        idempotency.markProcessed(eventId, Duration.ofDays(7));
    }
}