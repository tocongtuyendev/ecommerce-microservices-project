package com.yourcompany.ecommerce.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/internal/payments")
public class WebhookController {


    private final PaymentProcessor processor;


    public WebhookController(PaymentProcessor processor) {
        this.processor = processor;
    }


    @PostMapping("/webhook")
    public Mono<ResponseEntity<String>> receiveWebhook(@RequestHeader(value = "X-Signature", required = false) String signature,
                                                       @RequestBody String body) {
        return processor.handleWebhook(signature, body)
                .map(v -> ResponseEntity.ok("OK"))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }
}