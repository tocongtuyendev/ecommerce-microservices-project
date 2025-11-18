package com.yourcompany.ecommerce.product.service;

import com.yourcompany.ecommerce.product.dto.ProductRequest;
import com.yourcompany.ecommerce.product.dto.ProductResponse;
import com.yourcompany.ecommerce.product.exception.ProductNotFoundException;
import com.yourcompany.ecommerce.product.mapper.ProductMapper;
import com.yourcompany.ecommerce.product.model.Product;
import com.yourcompany.ecommerce.product.repository.ProductRepository;

import com.yourcompany.ecommerce.seller.dto.SellerResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public Flux<ProductResponse> getAllProducts() {
        return productRepository.findAll() // Trả về Flux<Product>
                .map(productMapper::toProductResponse); // Ánh xạ từng sản phẩm khi nó đi qua stream
    }

    public Mono<ProductResponse> getProductById(String id) {
        return productRepository.findById(id) // Trả về Mono<Product>
                .map(productMapper::toProductResponse);
    }

    private Mono<SellerResponse> getActiveSeller(Long userId) {
        return webClientBuilder.build().get()
                .uri("http://seller-management-service/api/sellers/by-user/{userId}", userId)
                .retrieve()
                .bodyToMono(SellerResponse.class)
                .flatMap(seller -> {
                    if (!"ACTIVE".equals(seller.getStatus())) {
                        return Mono.error(new RuntimeException("Seller is not active. Action denied."));
                    }
                    return Mono.just(seller);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Seller profile not found.")));
    }

    /**
     * Tạo một sản phẩm mới (chỉ dành cho seller active)
     */
    public Mono<ProductResponse> createProduct(ProductRequest productRequest, Long userId) {
        return getActiveSeller(userId)
                .flatMap(seller -> {
                    Product product = productMapper.toProduct(productRequest);
                    product.setSellerId(seller.getId()); // ID của Seller (String)
                    product.setSellerShopName(seller.getShopName());
                    return productRepository.save(product);
                })
                .map(productMapper::toProductResponse);
    }

    /**
     * Cập nhật một sản phẩm (chỉ dành cho chủ sở hữu sản phẩm)
     */
    public Mono<ProductResponse> updateProduct(String productId, ProductRequest productRequest, Long userId) {
        // Lấy thông tin người bán VÀ sản phẩm cần cập nhật cùng lúc
        Mono<SellerResponse> sellerMono = getActiveSeller(userId);
        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)));

        // Chỉ khi cả hai đều thành công, mới tiếp tục
        return Mono.zip(sellerMono, productMono)
                .flatMap(tuple -> {
                    SellerResponse seller = tuple.getT1();
                    Product product = tuple.getT2();

                    // KIỂM TRA QUYỀN SỞ HỮU
                    if (!product.getSellerId().equals(seller.getId())) {
                        return Mono.error(new RuntimeException("Forbidden: You do not own this product."));
                    }

                    // Nếu là chủ sở hữu, tiến hành cập nhật
                    productMapper.updateProductFromDto(productRequest, product);
                    return productRepository.save(product);
                })
                .map(productMapper::toProductResponse);
    }

    /**
     * Xóa một sản phẩm (chỉ dành cho chủ sở hữu sản phẩm)
     */
    public Mono<Void> deleteProduct(String productId, Long userId) {
        Mono<SellerResponse> sellerMono = getActiveSeller(userId);
        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)));

        return Mono.zip(sellerMono, productMono)
                .flatMap(tuple -> {
                    SellerResponse seller = tuple.getT1();
                    Product product = tuple.getT2();

                    // KIỂM TRA QUYỀN SỞ HỮU
                    if (!product.getSellerId().equals(seller.getId())) {
                        return Mono.error(new RuntimeException("Forbidden: You do not own this product."));
                    }

                    // Nếu là chủ sở hữu, tiến hành xóa
                    return productRepository.delete(product); // Trả về Mono<Void>
                });
    }
}