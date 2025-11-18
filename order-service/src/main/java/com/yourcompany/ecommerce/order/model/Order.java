package com.yourcompany.ecommerce.order.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {
    @Id
    private String id;
    private Long customerId; // ID của người mua
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED
    private BigDecimal totalAmount;
    private List<String> childOrderIds; // Liên kết đến các đơn hàng con
    private Instant createdAt;
}