package com.yourcompany.ecommerce.inventory.service;

import com.yourcompany.ecommerce.inventory.model.Inventory;
import com.yourcompany.ecommerce.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public void reduceStock(String skuCode, Integer quantity) {
        log.info("Attempting to reduce stock for skuCode: {} by quantity: {}", skuCode, quantity);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + skuCode));

        if (inventory.getQuantity() < quantity) {
            log.error("Stock not sufficient for product: {}. Required: {}, Available: {}", skuCode, quantity,
                    inventory.getQuantity());
            throw new RuntimeException("Insufficient stock for product: " + skuCode);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
        log.info("Stock updated successfully for skuCode: {}. New quantity: {}", skuCode, inventory.getQuantity());
    }
}
