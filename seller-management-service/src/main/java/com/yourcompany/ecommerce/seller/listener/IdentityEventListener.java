package com.yourcompany.ecommerce.seller.listener;

import com.yourcompany.ecommerce.common.event.SellerProfileCreateEvent;
import com.yourcompany.ecommerce.seller.model.Seller;
import com.yourcompany.ecommerce.seller.repository.SellerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdentityEventListener {

    @Autowired
    private SellerRepository sellerRepository;

    @RabbitListener(queues = "seller_management_queue")
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