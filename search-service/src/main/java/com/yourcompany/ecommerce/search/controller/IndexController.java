package com.yourcompany.ecommerce.search.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.ecommerce.search.service.EsSearchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/internal")
public class IndexController {

    private final EsSearchService esSearchService;
    private final ObjectMapper mapper = new ObjectMapper();

    public IndexController(EsSearchService esSearchService) {
        this.esSearchService = esSearchService;
    }

    @PostMapping(path = "/index/product", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> indexProduct(@RequestBody String body) {
        try {
            JsonNode node = mapper.readTree(body);
            String type = node.path("type").asText("");
            JsonNode payload = node.path("payload");
            String id = payload.path("id").asText(null);

            if (id == null || id.isEmpty()) {
                return Mono.error(new IllegalArgumentException("payload.id is required"));
            }

            if ("product.deleted".equals(type)) {
                return esSearchService.deleteDocument(id);
            } else {
                return esSearchService.indexDocument(id, payload);
            }
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
