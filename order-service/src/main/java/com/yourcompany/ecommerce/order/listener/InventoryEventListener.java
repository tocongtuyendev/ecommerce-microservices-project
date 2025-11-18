package com.yourcompany.ecommerce.order.listener;

import com.yourcompany.ecommerce.common.event.InventoryUpdateFailedEvent;
import com.yourcompany.ecommerce.common.event.InventoryUpdateSuccessEvent;
import com.yourcompany.ecommerce.order.config.KafkaConfig;
import com.yourcompany.ecommerce.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class InventoryEventListener {

    private final OrderRepository orderRepository;

    public InventoryEventListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Listen to both success and failed inventory topics; dispatch by payload type
    @KafkaListener(topics = {KafkaConfig.TOPIC_INVENTORY_SUCCESS, KafkaConfig.TOPIC_INVENTORY_FAILED}, containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handleInventoryEvents(Object event) {
        if (event instanceof InventoryUpdateSuccessEvent) {
            InventoryUpdateSuccessEvent success = (InventoryUpdateSuccessEvent) event;
            log.info("Received inventory update success for order: {}", success.getOrderNumber());
            orderRepository.findByOrderNumber(success.getOrderNumber()).ifPresent(order -> {
                if ("PENDING".equals(order.getStatus())) {
                    order.setStatus("CONFIRMED");
                    orderRepository.save(order);
                    log.info("Order {} status updated to CONFIRMED", order.getOrderNumber());
                }
            });

        } else if (event instanceof InventoryUpdateFailedEvent) {
            InventoryUpdateFailedEvent failed = (InventoryUpdateFailedEvent) event;
            log.warn("Received inventory update failure for order: {}. Reason: {}", failed.getOrderNumber(), failed.getReason());
            orderRepository.findByOrderNumber(failed.getOrderNumber()).ifPresent(order -> {
                if ("PENDING".equals(order.getStatus())) {
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                    log.warn("Order {} status updated to CANCELLED", order.getOrderNumber());
                }
            });
        } else {
            log.warn("Received unknown inventory event type: {}", event.getClass());
        }
    }
}
