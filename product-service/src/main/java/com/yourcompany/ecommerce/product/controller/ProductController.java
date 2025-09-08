package com.yourcompany.ecommerce.product.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.product.dto.ProductRequest;
import com.yourcompany.ecommerce.product.dto.ProductResponse;
import com.yourcompany.ecommerce.product.exception.ProductNotFoundException;
import com.yourcompany.ecommerce.product.service.ProductService;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Mono<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return productService.getAllProducts() // Trả về Flux<ProductResponse>
                .collectList() // Thu thập tất cả các phần tử thành Mono<List<ProductResponse>>
                .map(ApiResponse::success); // Bọc List trong ApiResponse
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(ApiResponse.success(product)))
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> createProduct(
            @RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest)
                .map(createdProduct -> new ResponseEntity<>(
                        ApiResponse.success("Product created successfully", createdProduct),
                        HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> updateProduct(@PathVariable String id,
            @RequestBody ProductRequest productRequest) {
        return productService.updateProduct(id, productRequest)
                .map(product -> ResponseEntity.ok(ApiResponse.success("Product updated successfully", product)))
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null))));
    }
}