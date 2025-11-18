package com.yourcompany.ecommerce.noti.listener;

import com.yourcompany.ecommerce.noti.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class EventListener {


    private final NotificationService service;


    public EventListener(NotificationService service) {
        this.service = service;
    }


    @KafkaListener(topics = "payment-events", groupId = "noti-service-group")
    public void onPaymentEvent(String message) {
        try {
            service.processEvent(message);
        } catch (Exception e) {
// log and let Kafka handle retry/DLQ
            System.err.println("Failed processing payment event: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = "order-events", groupId = "noti-service-group")
    public void onOrderEvent(String message) {
        try {
            service.processEvent(message);
        } catch (Exception e) {
            System.err.println("Failed processing order event: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @KafkaListener(topics = "shipment-events", groupId = "noti-service-group")
    public void onShipmentEvent(String message) {
        try {
            service.processEvent(message);
        } catch (Exception e) {
            System.err.println("Failed processing shipment event: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}