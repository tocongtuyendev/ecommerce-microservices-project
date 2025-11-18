package com.yourcompany.ecommerce.payment.controller;

import com.yourcompany.ecommerce.payment.dto.CreatePaymentRequest;
import com.yourcompany.ecommerce.payment.service.PaymentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PaymentProcessor processor;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> createPayment(@Valid @RequestBody CreatePaymentRequest req) {
        reactor.core.publisher.Mono<java.util.Map<String, Object>> result = processor.createPayment(req);

        java.util.function.Function<java.util.Map<String, Object>, ResponseEntity<?>> toResponse = new java.util.function.Function<java.util.Map<String, Object>, ResponseEntity<?>>() {
            @Override
            public ResponseEntity<?> apply(java.util.Map<String, Object> resp) {
                return ResponseEntity.accepted().body(resp);
            }
        };

        return result.map(toResponse)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }
}
