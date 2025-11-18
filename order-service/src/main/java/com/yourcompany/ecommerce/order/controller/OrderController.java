package com.yourcompany.ecommerce.order.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.order.dto.OrderRequest;
import com.yourcompany.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<String>>> placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest)
                .map(orderNumber -> {
                    ApiResponse<String> response = ApiResponse.success("Order placed successfully. Order Number: " + orderNumber, orderNumber);
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                });
    }
}
