package com.yourcompany.ecommerce.inventory.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.inventory.model.Inventory;
import com.yourcompany.ecommerce.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping
    public ApiResponse<Inventory> addStock(@RequestBody Inventory inventory) {
        return ApiResponse.success("Stock added", inventoryRepository.save(inventory));
    }

    @GetMapping("/{skuCode}")
    public ApiResponse<Integer> getStock(@PathVariable String skuCode) {
        Integer quantity = inventoryRepository.findBySkuCode(skuCode)
                .map(Inventory::getQuantity)
                .orElse(0);
        return ApiResponse.success(quantity);
    }
}
