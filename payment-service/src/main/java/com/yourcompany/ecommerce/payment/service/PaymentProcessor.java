package com.yourcompany.ecommerce.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.ecommerce.payment.dto.CreatePaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Mono;


import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Service
public class PaymentProcessor {


    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();
    private final IdempotencyService idempotency;


    @Value("${app.kafka.topic}")
    private String topic;


    @Value("${app.payment.provider.secret}")
    private String providerSecret;


    public PaymentProcessor(KafkaTemplate<String, String> kafka, IdempotencyService idempotency) {
        this.kafka = kafka;
        this.idempotency = idempotency;
    }


    /**
     * Simulate creating a payment request to external provider.
     * Returns a redirect URL (mock) that the client should open to pay.
     */
    public Mono<Map<String, Object>> createPayment(CreatePaymentRequest req) {
        return Mono.fromSupplier(() -> {
            String paymentId = "pay-" + UUID.randomUUID().toString();
            Map<String, Object> resp = new HashMap<>();
// In real world, call provider API and get checkout url
            resp.put("paymentId", paymentId);
            resp.put("checkoutUrl", "https://sandbox.example-gateway.local/checkout?paymentId=" + paymentId + "&amount=" + req.getAmount());


// publish a 'payment.created' event for downstream systems (optional)
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("eventId", UUID.randomUUID().toString());
                event.put("type", "payment.created");
                event.put("payload", req);
                kafka.send(topic, mapper.writeValueAsString(event));
            } catch (Exception e) {
// log but continue
                System.err.println("Failed to publish payment.created: " + e.getMessage());
            }


            return resp;
        });
    }


    /**
     * Handle incoming webhook from payment provider. Verify signature, ensure idempotency, and publish result to Kafka.
     */
    public Mono<Void> handleWebhook(String signature, String body) {
        return Mono.fromRunnable(() -> {
// simple signature verification (HMAC-like using providerSecret). In real world use HMAC-SHA256.
            String computed = DigestUtils.md5DigestAsHex((body + providerSecret).getBytes(StandardCharsets.UTF_8));
            if (signature == null || !signature.equals(computed)) {
                throw new RuntimeException("Invalid signature");
            }


// parse payload
            try {
                Map payload = mapper.readValue(body, Map.class);
                String eventId = (String) payload.get("eventId");
                if (eventId == null) {
                    throw new RuntimeException("missing eventId");
                }


// idempotency check
                if (idempotency.isProcessed(eventId)) {
                    return;
                }


                String status = (String) payload.getOrDefault("status", "unknown");
                Map<String, Object> event = new HashMap<>();
                event.put("eventId", eventId);
                event.put("type", status.equalsIgnoreCase("success") ? "payment.succeeded" : "payment.failed");
                event.put("payload", payload);


                kafka.send(topic, mapper.writeValueAsString(event));


// mark processed for 7 days
                idempotency.markProcessed(eventId, Duration.ofDays(7));


            } catch (Exception e) {
                throw new RuntimeException("failed to process webhook: " + e.getMessage());
            }
        });
    }
}