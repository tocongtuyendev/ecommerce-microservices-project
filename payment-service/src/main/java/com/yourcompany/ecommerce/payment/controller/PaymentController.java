package com.yourcompany.ecommerce.payment.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


import javax.validation.Valid;


@RestController
@RequestMapping("/api/v1/payments")
@Validated
public class PaymentController {


    private final PaymentProcessor processor;


    public PaymentController(PaymentProcessor processor) {
        this.processor = processor;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> createPayment(@Valid @RequestBody CreatePaymentRequest req) {
        return processor.createPayment(req)
                .map(resp -> ResponseEntity.accepted().body(resp))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }
}
