package com.yourcompany.ecommerce.order.service;

import com.yourcompany.ecommerce.common.event.OrderPlacedEvent;
import com.yourcompany.ecommerce.order.config.KafkaConfig;
import com.yourcompany.ecommerce.order.dto.OrderItemRequest;
import com.yourcompany.ecommerce.order.dto.OrderRequest;
import com.yourcompany.ecommerce.order.model.Order;
import com.yourcompany.ecommerce.order.model.OrderItem;
import com.yourcompany.ecommerce.order.model.ChildOrder;
import com.yourcompany.ecommerce.order.repository.OrderRepository;
import com.yourcompany.ecommerce.order.repository.ChildOrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ChildOrderRepository childOrderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Mono<String> placeOrder(OrderRequest orderRequest) {
        // Basic validation
        if (orderRequest == null || orderRequest.getOrderItems() == null || orderRequest.getOrderItems().isEmpty()) {
            return Mono.error(new IllegalArgumentException("OrderRequest must contain at least one order item"));
        }

    // 1. Create parent Order entity and set basic properties
    Order order = new Order();
    order.setOrderNumber(UUID.randomUUID().toString());
    order.setStatus("PENDING");
    order.setCreatedAt(java.time.Instant.now());

        // 2. Map DTO -> Entity
        List<OrderItem> orderItems = orderRequest.getOrderItems()
                .stream()
                .filter(Objects::nonNull)
                .map(this::mapToOrderItemEntity)
                .collect(Collectors.toList());

        // validate sellerId presence for C2C split
        boolean anyMissingSeller = orderItems.stream().anyMatch(i -> i.getSellerId() == null || i.getSellerId().isEmpty());
        if (anyMissingSeller) {
            return Mono.error(new IllegalArgumentException("All order items must include sellerId for marketplace (C2C) flow"));
        }

        // group items by sellerId to create ChildOrders
        java.util.Map<String, List<OrderItem>> itemsBySeller = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getSellerId));

        // persist parent, then create and persist child orders, update parent and publish event
        return orderRepository.save(order)
                .flatMap(savedParent -> {
                    List<ChildOrder> childOrders = itemsBySeller.entrySet().stream().map(e -> {
                        ChildOrder co = new ChildOrder();
                        co.setParentOrderId(savedParent.getId());
                        co.setSellerId(e.getKey());
                        co.setStatus("PENDING");
                        co.setItems(e.getValue());
                        java.math.BigDecimal subTotal = e.getValue().stream()
                                .map(i -> i.getPrice().multiply(new java.math.BigDecimal(i.getQuantity())))
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                        co.setSubTotal(subTotal);
                        return co;
                    }).collect(Collectors.toList());

                    return reactor.core.publisher.Flux.fromIterable(childOrders)
                            .flatMap(childOrderRepository::save)
                            .collectList()
                            .flatMap(savedChildren -> {
                                List<String> childIds = savedChildren.stream().map(ChildOrder::getId).collect(Collectors.toList());
                                savedParent.setChildOrderIds(childIds);
                                java.math.BigDecimal total = savedChildren.stream()
                                        .map(ChildOrder::getSubTotal)
                                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                                savedParent.setTotalAmount(total);
                                return orderRepository.save(savedParent)
                                        .doOnSuccess(finalParent -> {
                                            try {
                                                List<OrderPlacedEvent.OrderItemData> eventItems = orderItems.stream()
                                                        .map(i -> new OrderPlacedEvent.OrderItemData(i.getSkuCode(), i.getQuantity()))
                                                        .collect(Collectors.toList());
                                                OrderPlacedEvent event = new OrderPlacedEvent(finalParent.getOrderNumber(), eventItems);
                                                kafkaTemplate.send(KafkaConfig.TOPIC_ORDER_PLACED, event);
                                                log.info("Published OrderPlacedEvent for order {}", finalParent.getOrderNumber());
                                            } catch (Exception e) {
                                                log.error("Failed to publish OrderPlacedEvent for order {}", finalParent.getOrderNumber(), e);
                                            }
                                        })
                                        .map(Order::getOrderNumber);
                            });
                });
    }

    private OrderItem mapToOrderItemEntity(OrderItemRequest itemRequest) {
        OrderItem orderItem = new OrderItem();
        orderItem.setPrice(itemRequest.getPrice());
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setSkuCode(itemRequest.getSkuCode());
        orderItem.setSellerId(itemRequest.getSellerId());
        return orderItem;
    }
}
