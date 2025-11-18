package com.yourcompany.ecommerce.inventory.repository;

import com.yourcompany.ecommerce.inventory.model.Inventory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveMongoRepository<Inventory, String> {
    // Tìm kho hàng cho một sản phẩm cụ thể của một người bán cụ thể
    Mono<Inventory> findBySellerIdAndProductId(String sellerId, String productId);
}