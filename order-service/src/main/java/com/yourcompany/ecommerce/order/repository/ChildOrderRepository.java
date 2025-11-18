package com.yourcompany.ecommerce.order.repository;

import com.yourcompany.ecommerce.order.model.ChildOrder;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildOrderRepository extends ReactiveMongoRepository<ChildOrder, String> {
}
