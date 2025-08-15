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

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> placeOrder(@RequestBody OrderRequest orderRequest) {
        String orderNumber = orderService.placeOrder(orderRequest);
        ApiResponse<String> response = ApiResponse.success("Order placed successfully. Order Number: " + orderNumber, orderNumber);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
