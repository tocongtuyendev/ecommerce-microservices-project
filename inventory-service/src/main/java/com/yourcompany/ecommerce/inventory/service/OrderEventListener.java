package com.yourcompany.ecommerce.inventory.service;

import com.yourcompany.ecommerce.common.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    private InventoryService inventoryService;

    @RabbitListener(queues = "inventory_queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received order placed event: {}", event.getOrderNumber());

        try {
            for (OrderPlacedEvent.OrderItemData item : event.getOrderItems()) {
                inventoryService.reduceStock(item.getProductSku(), item.getQuantity());
            }
            log.info("Inventory updated successfully for order: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to update inventory for order: {}. Reason: {}", event.getOrderNumber(), e.getMessage());
            // Trong một hệ thống thực tế, bạn sẽ cần một cơ chế để xử lý lỗi này,
            // ví dụ như gửi sự kiện `InventoryUpdateFailedEvent` để `order-service` có thể
            // hoàn tác đơn hàng.
        }
    }
}
