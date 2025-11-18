package com.yourcompany.ecommerce.inventory.service;

import com.yourcompany.ecommerce.inventory.exception.InsufficientStockException;
import com.yourcompany.ecommerce.inventory.exception.InventoryNotFoundException;
import com.yourcompany.ecommerce.inventory.model.Inventory;
import com.yourcompany.ecommerce.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Reduce stock for a given sellerId + productId in a reactive, optimistic-locking-safe way.
     * Returns the saved Inventory on success or an error Mono on failure.
     */
    public Mono<Inventory> reduceStock(String sellerId, String productId, Integer quantityToReduce) {
        log.info("Attempting to reduce stock for productId: {} sellerId: {} by quantity: {}", productId, sellerId, quantityToReduce);

        if (quantityToReduce == null || quantityToReduce <= 0) {
            return Mono.error(new IllegalArgumentException("quantityToReduce must be > 0"));
        }

        return inventoryRepository.findBySellerIdAndProductId(sellerId, productId)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("Inventory not found for productId: " + productId + " and sellerId: " + sellerId)))
                .flatMap(inventory -> {
                    if (inventory.getQuantity() == null || inventory.getQuantity() < quantityToReduce) {
                        return Mono.error(new InsufficientStockException("Insufficient stock for productId: " + productId + ", required: " + quantityToReduce + ", available: " + inventory.getQuantity()));
                    }
                    inventory.setQuantity(inventory.getQuantity() - quantityToReduce);
                    return inventoryRepository.save(inventory);
                })
                // Retry a few times in case of optimistic locking failures
                .retryWhen(Retry.backoff(3, Duration.ofMillis(50))
                        .filter(throwable -> throwable instanceof OptimisticLockingFailureException))
                .doOnSuccess(inv -> log.info("Stock updated successfully for productId: {} sellerId: {}. New quantity: {}", productId, sellerId, inv.getQuantity()))
                .doOnError(err -> log.error("Failed to reduce stock for productId: {} sellerId: {}: {}", productId, sellerId, err.getMessage()));
    }

    /**
     * Convenience helper for event listeners when only productId/sku is available.
     * It will try to find inventory by productId and reduce stock.
     */
    public Mono<Inventory> reduceStockByProductId(String productId, Integer quantityToReduce) {
        // Delegate to repository.findByProductId for backwards compatibility with events that only include sku/productId
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("Inventory not found for productId: " + productId)))
                .flatMap(inventory -> {
                    if (quantityToReduce == null || quantityToReduce <= 0) {
                        return Mono.error(new IllegalArgumentException("quantityToReduce must be > 0"));
                    }
                    if (inventory.getQuantity() == null || inventory.getQuantity() < quantityToReduce) {
                        return Mono.error(new InsufficientStockException("Insufficient stock for productId: " + productId + ", required: " + quantityToReduce + ", available: " + inventory.getQuantity()));
                    }
                    inventory.setQuantity(inventory.getQuantity() - quantityToReduce);
                    return inventoryRepository.save(inventory);
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(50))
                        .filter(throwable -> throwable instanceof OptimisticLockingFailureException));
    }
}
