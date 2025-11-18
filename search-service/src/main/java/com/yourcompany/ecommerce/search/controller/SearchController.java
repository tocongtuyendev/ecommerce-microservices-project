package com.yourcompany.ecommerce.search.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.ecommerce.search.model.ProductIndex;
import com.yourcompany.ecommerce.search.service.CacheService;
import com.yourcompany.ecommerce.search.service.EsSearchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/search")
public class SearchController {


    private final EsSearchService esSearch;
    private final CacheService cache;
    private final ObjectMapper mapper = new ObjectMapper();


    public SearchController(EsSearchService esSearch, CacheService cache) {
        this.esSearch = esSearch;
        this.cache = cache;
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<ProductIndex>> search(@RequestParam("q") String q,
                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        String cacheKey = "search:v1:" + q + ":" + page + ":" + size;


        return cache.get(cacheKey)
                .flatMap(cached -> {
                    try {
                        List<ProductIndex> cachedList = mapper.readValue(cached, mapper.getTypeFactory().constructCollectionType(List.class, ProductIndex.class));
                        return Mono.just(cachedList);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(
                        esSearch.search(q, page, size)
                                .collectList()
                                .flatMap(list -> {
                                    try {
                                        String serialized = mapper.writeValueAsString(list);
                                        return cache.put(cacheKey, serialized, Duration.ofSeconds(30))
                                                .flatMap(ok -> Mono.just(list));
                                    } catch (Exception e) {
                                        return Mono.error(e);
                                    }
                                })
                );
    }
}