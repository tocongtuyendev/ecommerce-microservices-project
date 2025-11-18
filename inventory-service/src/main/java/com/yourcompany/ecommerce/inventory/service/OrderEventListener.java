package com.yourcompany.ecommerce.inventory.service;

import com.yourcompany.ecommerce.common.event.InventoryUpdateFailedEvent;
import com.yourcompany.ecommerce.common.event.InventoryUpdateSuccessEvent;
import com.yourcompany.ecommerce.common.event.OrderPlacedEvent;
import com.yourcompany.ecommerce.inventory.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConfig.TOPIC_ORDER_PLACED, containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received order placed event: {}", event.getOrderNumber());
        // Process items reactively. We use the productSku as productId/sku when looking up inventory.
        Flux.fromIterable(event.getOrderItems())
                // process sequentially to avoid overloading DB with concurrent updates for same product
                .publishOn(Schedulers.boundedElastic())
                .concatMap(item -> inventoryService.reduceStockByProductId(item.getProductSku(), item.getQuantity()))
                .collectList()
                .subscribe(savedInventories -> {
                    // All items processed successfully
                    InventoryUpdateSuccessEvent successEvent = new InventoryUpdateSuccessEvent(event.getOrderNumber());
                    kafkaTemplate.send(KafkaConfig.TOPIC_INVENTORY_SUCCESS, successEvent);
                    log.info("Sent inventory update SUCCESS event for order: {}", event.getOrderNumber());
                }, err -> {
                    // On error send failed event with reason
                    log.error("Inventory update FAILED for order: {}. Reason: {}", event.getOrderNumber(), err.getMessage());
                    InventoryUpdateFailedEvent failedEvent = new InventoryUpdateFailedEvent(event.getOrderNumber(), err.getMessage());
                    kafkaTemplate.send(KafkaConfig.TOPIC_INVENTORY_FAILED, failedEvent);
                    log.info("Sent inventory update FAILED event for order: {}", event.getOrderNumber());
                });
    }
}
