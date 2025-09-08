package com.yourcompany.ecommerce.product.service;

import com.yourcompany.ecommerce.product.dto.ProductRequest;
import com.yourcompany.ecommerce.product.dto.ProductResponse;
import com.yourcompany.ecommerce.product.mapper.ProductMapper;
import com.yourcompany.ecommerce.product.model.Product;
import com.yourcompany.ecommerce.product.repository.ProductRepository;

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

    public Flux<ProductResponse> getAllProducts() {
        return productRepository.findAll() // Trả về Flux<Product>
                .map(productMapper::toProductResponse); // Ánh xạ từng sản phẩm khi nó đi qua stream
    }

    public Mono<ProductResponse> getProductById(String id) {
        return productRepository.findById(id) // Trả về Mono<Product>
                .map(productMapper::toProductResponse);
    }

    public Mono<ProductResponse> createProduct(ProductRequest productRequest) {
        Product product = productMapper.toProduct(productRequest);
        return productRepository.save(product) // Trả về Mono<Product>
                .map(productMapper::toProductResponse);
    }

    public Mono<ProductResponse> updateProduct(String id, ProductRequest productRequest) {
        return productRepository.findById(id) // 1. Bắt đầu một luồng bất đồng bộ để tìm sản phẩm
                .flatMap(existingProduct -> { // 2. Nếu tìm thấy, thực hiện một hành động bất đồng bộ khác
                    productMapper.updateProductFromDto(productRequest, existingProduct);
                    return productRepository.save(existingProduct); // 3. `save` cũng là bất đồng bộ, trả về
                                                                    // Mono<Product>
                })
                .map(productMapper::toProductResponse); // 4. Ánh xạ kết quả cuối cùng sang DTO
    }

    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id); // Trả về Mono<Void> để báo hiệu hoàn thành
    }
}