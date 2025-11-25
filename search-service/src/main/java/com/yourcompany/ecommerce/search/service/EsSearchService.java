package com.yourcompany.ecommerce.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.ecommerce.search.model.ProductIndex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class EsSearchService {


    private final WebClient es;
    private final String index;
    private final ObjectMapper mapper = new ObjectMapper();


    public EsSearchService(WebClient elasticsearchWebClient, @Value("${app.elasticsearch.index}") String index) {
        this.es = elasticsearchWebClient;
        this.index = index;
    }


    /**
     * Simple full-text search using ES _search API (multi_match on name & description)
     */
    public Flux<ProductIndex> search(String q, int page, int size) {
        int from = page * size;
        String body = "{\"query\":{\"bool\":{\"must\":{\"multi_match\":{\"query\":\"" + escape(q) + "\",\"fields\":[\"name^3\",\"description\"]}}}},\"from\":" + from + ",\"size\":" + size + "}";


        return es.post()
                .uri(uriBuilder -> uriBuilder.path("/" + index + "/_search").build())
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(this::parseSearchResponse);
    }


    /**
     * Index (create or update) a document in ES using product id as document id.
     */
    public Mono<String> indexDocument(String id, Object payload) {
        try {
            String body = mapper.writeValueAsString(payload);
            String docId = URLEncoder.encode(id, StandardCharsets.UTF_8);
            return es.put()
                    .uri(uriBuilder -> uriBuilder.path("/" + index + "/_doc/" + docId).build())
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(String.class);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     * Delete a document by id from ES
     */
    public Mono<String> deleteDocument(String id) {
        String docId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        return es.delete()
                .uri(uriBuilder -> uriBuilder.path("/" + index + "/_doc/" + docId).build())
                .retrieve()
                .bodyToMono(String.class);
    }


    private String escape(String q) {
        return q.replace("\"", "\\\"");
    }


    private Flux<ProductIndex> parseSearchResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode hits = root.path("hits").path("hits");
            List<ProductIndex> list = new ArrayList<>();
            for (JsonNode hit : hits) {
                JsonNode src = hit.path("_source");
                ProductIndex p = mapper.treeToValue(src, ProductIndex.class);
                list.add(p);
            }
            return Flux.fromIterable(list);
        } catch (IOException e) {
            return Flux.error(e);
        }
    }
}