package com.yourcompany.ecommerce.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    private String sellerId;

}
