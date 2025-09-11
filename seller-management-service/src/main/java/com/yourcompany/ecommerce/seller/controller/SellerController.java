package com.yourcompany.ecommerce.seller.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.seller.dto.SellerApprovalRequest;
import com.yourcompany.ecommerce.seller.dto.SellerResponse;
import com.yourcompany.ecommerce.seller.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponse<List<SellerResponse>>> getSellers(@RequestParam String status) {
        return sellerService.getAllSellersByStatus(status.toUpperCase())
                .collectList()
                .map(ApiResponse::success);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<SellerResponse>>> getSellerById(@PathVariable String id) {
        return sellerService.getSellerById(id)
                .map(seller -> ResponseEntity.ok(ApiResponse.success(seller)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<SellerResponse>>> processApproval(@PathVariable String id, @RequestBody SellerApprovalRequest approvalRequest) {
        return sellerService.processSellerApproval(id, approvalRequest)
                .map(updatedSeller -> ResponseEntity.ok(ApiResponse.success("Seller status updated", updatedSeller)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}