package com.yourcompany.ecommerce.noti.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/internal/noti")
public class AdminController {

    // Admin endpoint that receives a raw event and processes it immediately (useful for testing)
    @PostMapping("/event")
    public ResponseEntity<String> receiveEvent(@RequestBody String body) {
// For simplicity, directly call NotificationService via Kafka in production. Here, we accept raw event for testing.
// In this template we'll simply return OK and rely on Kafka-based flow in real usage.
        return ResponseEntity.accepted().body("received");
    }
}