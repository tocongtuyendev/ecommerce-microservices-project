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
    public void reduceStock(String skuCode, Integer quantityToReduce) {
        log.info("Attempting to reduce stock for skuCode: {} by quantity: {}", skuCode, quantityToReduce);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(
                        () -> new RuntimeException("Product with skuCode " + skuCode + " not found in inventory."));

        if (inventory.getQuantity() < quantityToReduce) {
            // Ném ra exception nếu không đủ hàng
            throw new RuntimeException("Insufficient stock for skuCode: " + skuCode + ". Required: " + quantityToReduce
                    + ", Available: " + inventory.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToReduce);
        inventoryRepository.save(inventory);
        log.info("Stock updated successfully for skuCode: {}. New quantity: {}", skuCode, inventory.getQuantity());
    }
}
