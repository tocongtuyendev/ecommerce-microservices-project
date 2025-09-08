package com.yourcompany.ecommerce.product.repository;

import com.yourcompany.ecommerce.product.model.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
