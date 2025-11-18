package com.yourcompany.ecommerce.order.repository;

import com.yourcompany.ecommerce.order.model.Order;

import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Mono<Order> findByOrderNumber(String orderNumber);
}
