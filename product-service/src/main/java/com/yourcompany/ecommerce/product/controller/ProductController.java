package com.yourcompany.ecommerce.product.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.product.dto.ProductRequest;
import com.yourcompany.ecommerce.product.dto.ProductResponse;
import com.yourcompany.ecommerce.product.exception.ProductNotFoundException;
import com.yourcompany.ecommerce.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Endpoint công khai, ai cũng có thể xem
    @GetMapping
    public Mono<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return productService.getAllProducts()
                .collectList()
                .map(ApiResponse::success);
    }

    // Endpoint công khai, ai cũng có thể xem
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(ApiResponse.success(product)))
                // Lỗi này sẽ được GlobalExceptionHandler bắt lại
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> createProduct(
            @RequestBody ProductRequest productRequest,
            @RequestHeader("X-User-Id") Long userId) {

        return productService.createProduct(productRequest, userId)
                .map(createdProduct -> new ResponseEntity<>(
                        ApiResponse.success("Product created successfully", createdProduct),
                        HttpStatus.CREATED))
                .onErrorResume(RuntimeException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()))
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Mono<ResponseEntity<ApiResponse<ProductResponse>>> updateProduct(
            @PathVariable String id,
            @RequestBody ProductRequest productRequest,
            @RequestHeader("X-User-Id") Long userId) {

        return productService.updateProduct(id, productRequest, userId)
                .map(product -> ResponseEntity.ok(ApiResponse.success("Product updated successfully", product)))
                .onErrorResume(ProductNotFoundException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()))
                ))
                .onErrorResume(RuntimeException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()))
                ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')") // Admin cũng có thể xóa sản phẩm
    public Mono<ResponseEntity<ApiResponse<Object>>> deleteProduct(
            @PathVariable String id,
            @RequestHeader("X-User-Id") Long userId) {

        return productService.deleteProduct(id, userId)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null))))
                .onErrorResume(ProductNotFoundException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()))
                ))
                .onErrorResume(RuntimeException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()))
                ));
    }
}