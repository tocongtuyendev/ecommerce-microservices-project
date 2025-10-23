package com.yourcompany.ecommerce.order.listener;

import com.yourcompany.ecommerce.common.event.InventoryUpdateFailedEvent;
import com.yourcompany.ecommerce.common.event.InventoryUpdateSuccessEvent;
import com.yourcompany.ecommerce.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class InventoryEventListener {

    @Autowired
    private OrderRepository orderRepository;

    // Listener này CHỈ xử lý sự kiện thành công
    @RabbitListener(queues = "order_queue")
    @Transactional
    public void handleInventoryUpdateSuccess(InventoryUpdateSuccessEvent event) {
        log.info("Received inventory update success for order: {}", event.getOrderNumber());
        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresent(order -> {
            if ("PENDING".equals(order.getOrderStatus())) {
                order.setOrderStatus("CONFIRMED");
                orderRepository.save(order);
                log.info("Order {} status updated to CONFIRMED", order.getOrderNumber());
            }
        });
    }

    // Listener này CHỈ xử lý sự kiện thất bại
    @RabbitListener(queues = "order_queue")
    @Transactional
    public void handleInventoryUpdateFailure(InventoryUpdateFailedEvent event) {
        log.warn("Received inventory update failure for order: {}. Reason: {}", event.getOrderNumber(),
                event.getReason());
        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresent(order -> {
            if ("PENDING".equals(order.getOrderStatus())) {
                order.setOrderStatus("CANCELLED");
                orderRepository.save(order);
                log.warn("Order {} status updated to CANCELLED", order.getOrderNumber());
            }
        });
    }
}
