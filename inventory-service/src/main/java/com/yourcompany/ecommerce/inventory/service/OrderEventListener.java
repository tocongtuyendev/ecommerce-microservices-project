package com.yourcompany.ecommerce.inventory.service;

import com.yourcompany.ecommerce.common.event.InventoryUpdateFailedEvent;
import com.yourcompany.ecommerce.common.event.InventoryUpdateSuccessEvent;
import com.yourcompany.ecommerce.common.event.OrderPlacedEvent;
import com.yourcompany.ecommerce.inventory.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_QUEUE_NAME)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received order placed event: {}", event.getOrderNumber());

        try {
            // Thử thực hiện việc trừ kho
            for (OrderPlacedEvent.OrderItemData item : event.getOrderItems()) {
                inventoryService.reduceStock(item.getProductSku(), item.getQuantity());
            }

            // Nếu không có lỗi, gửi sự kiện THÀNH CÔNG
            InventoryUpdateSuccessEvent successEvent = new InventoryUpdateSuccessEvent(event.getOrderNumber());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_SUCCESS,
                    successEvent);
            log.info("Sent inventory update SUCCESS event for order: {}", event.getOrderNumber());

        } catch (RuntimeException e) {
            // Nếu có lỗi (hết hàng, sản phẩm không tồn tại), gửi sự kiện THẤT BẠI
            log.error("Inventory update FAILED for order: {}. Reason: {}", event.getOrderNumber(), e.getMessage());
            InventoryUpdateFailedEvent failedEvent = new InventoryUpdateFailedEvent(event.getOrderNumber(),
                    e.getMessage());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_FAILED, failedEvent);
            log.info("Sent inventory update FAILED event for order: {}", event.getOrderNumber());
        }
    }
}
