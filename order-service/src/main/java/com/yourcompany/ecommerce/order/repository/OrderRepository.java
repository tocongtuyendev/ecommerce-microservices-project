package com.yourcompany.ecommerce.order.repository;

import com.yourcompany.ecommerce.order.model.Order;

import java.util.Optional;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
}
