package com.yourcompany.ecommerce.sync.listener;

import com.yourcompany.ecommerce.sync.service.SyncService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventListener {

    private final SyncService syncService;

    public ProductEventListener(SyncService syncService) {
        this.syncService = syncService;
    }

    @KafkaListener(topics = "product-events", groupId = "sync-service-group")
    public void onProductEvent(String message) {
        try {
            syncService.handleEvent(message);
        } catch (Exception e) {
// If exception thrown after retries, log and allow DLQ handling via Kafka (or implement manual DLQ)
// For production, integrate a DLQ or use spring-kafka error handler
            System.err.println("Failed to process event: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}