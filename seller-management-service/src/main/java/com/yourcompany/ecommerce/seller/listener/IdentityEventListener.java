package com.yourcompany.ecommerce.seller.listener;

import com.yourcompany.ecommerce.common.event.SellerProfileCreateEvent;
import com.yourcompany.ecommerce.seller.config.KafkaConfig;
import com.yourcompany.ecommerce.seller.model.Seller;
import com.yourcompany.ecommerce.seller.repository.SellerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdentityEventListener {

    private final SellerRepository sellerRepository;

    public IdentityEventListener(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_SELLER_REGISTERED, containerFactory = "kafkaListenerContainerFactory")
    public void handleSellerRegistration(SellerProfileCreateEvent event) {
        log.info("Received seller registration request for userId: {}", event.getUserId());

        Seller newSeller = new Seller();
        newSeller.setUserId(event.getUserId());
        newSeller.setUsername(event.getUsername());
        newSeller.setShopName(event.getUsername() + "'s Shop"); // Tên mặc định
        newSeller.setStatus("PENDING_APPROVAL"); // Trạng thái ban đầu

        sellerRepository.save(newSeller)
                .doOnSuccess(savedSeller -> log.info("Successfully created seller profile for userId: {}", savedSeller.getUserId()))
                .doOnError(error -> log.error("Failed to create seller profile for userId: {}", event.getUserId(), error))
                .subscribe();
    }
}