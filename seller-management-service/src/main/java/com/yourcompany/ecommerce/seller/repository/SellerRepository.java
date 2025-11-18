package com.yourcompany.ecommerce.seller.repository;

import com.netflix.appinfo.ApplicationInfoManager;
import com.yourcompany.ecommerce.seller.model.Seller;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SellerRepository extends ReactiveMongoRepository<Seller, String> {
    Flux<Seller> findAllByStatus(String status);
    Mono<Seller> findByUserId(Long userId);
}