package com.yourcompany.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent implements Serializable {
    private String orderNumber;
    private List<OrderItemData> orderItems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemData implements Serializable {
        private String productSku; // Hoặc ID sản phẩm
        private Integer quantity;
    }
}
