package com.yourcompany.ecommerce.order.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "child_orders")
@Data
public class ChildOrder {
    @Id
    private String id;
    private String parentOrderId; // Liên kết về đơn hàng cha
    private String sellerId; // Người bán chịu trách nhiệm
    private String status; // PENDING, CONFIRMED, SHIPPED, CANCELLED
    private List<OrderItem> items;
    private BigDecimal subTotal;
}