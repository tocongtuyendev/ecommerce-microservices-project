package com.yourcompany.ecommerce.order.service;

import com.yourcompany.ecommerce.common.event.OrderPlacedEvent;
import com.yourcompany.ecommerce.order.config.RabbitMQConfig;
import com.yourcompany.ecommerce.order.dto.OrderItemRequest;
import com.yourcompany.ecommerce.order.dto.OrderRequest;
import com.yourcompany.ecommerce.order.model.Order;
import com.yourcompany.ecommerce.order.model.OrderItem;
import com.yourcompany.ecommerce.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public String placeOrder(OrderRequest orderRequest) {
        // 1. Tạo đối tượng Order mới
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        // 2. Chuyển đổi từ List<OrderItemRequest> (DTO) sang List<OrderItem> (Entity)
        List<OrderItem> orderItems = orderRequest.getOrderItems()
               .stream()
               .map(this::mapToOrderItemEntity)
               .collect(Collectors.toList());

        // 3. Thiết lập mối quan hệ hai chiều
        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        // 4. Lưu Order vào database (OrderItem cũng sẽ được lưu theo nhờ CascadeType.ALL)
        orderRepository.save(order);

        // 5. Tạo đối tượng sự kiện OrderPlacedEvent từ thông tin đơn hàng
        List<OrderPlacedEvent.OrderItemData> eventItems = order.getOrderItems().stream()
               .map(item -> new OrderPlacedEvent.OrderItemData(item.getSkuCode(), item.getQuantity()))
               .collect(Collectors.toList());
        OrderPlacedEvent event = new OrderPlacedEvent(order.getOrderNumber(), eventItems);

        // 6. Gửi sự kiện đến RabbitMQ
        //    - Gửi đến exchange 'order_exchange'
        //    - Với routing key là 'order.placed'
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);

        return order.getOrderNumber();
    }

    private OrderItem mapToOrderItemEntity(OrderItemRequest itemRequest) {
        OrderItem orderItem = new OrderItem();
        orderItem.setPrice(itemRequest.getPrice());
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setSkuCode(itemRequest.getSkuCode());
        return orderItem;
    }
}
