package com.yourcompany.ecommerce.order.repository;

import com.yourcompany.ecommerce.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
