package com.yourcompany.ecommerce.inventory.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventory")
@Data
public class Inventory {
    @Id
    private String id;
    private String productId; // ID sản phẩm (từ product-service)
    private String sellerId; // ID người bán (từ seller-management-service)
    private Integer quantity;
    private Integer reservedQuantity; // Số lượng đang được tạm giữ (cho Saga)
}