package com.yourcompany.ecommerce.inventory.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.inventory.model.Inventory;
import com.yourcompany.ecommerce.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping
    public Mono<ApiResponse<Mono<Inventory>>> addStock(@RequestBody Inventory inventory) {
        return Mono.fromCallable(() -> inventoryRepository.save(inventory))
                .subscribeOn(Schedulers.boundedElastic())
                .map(saved -> ApiResponse.success("Stock added", saved));
    }

}
