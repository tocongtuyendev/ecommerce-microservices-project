
package com.yourcompany.ecommerce.product.service;

import com.yourcompany.ecommerce.product.dto.ProductRequest;
import com.yourcompany.ecommerce.product.dto.ProductResponse;
import com.yourcompany.ecommerce.product.mapper.ProductMapper;
import com.yourcompany.ecommerce.product.model.Product;
import com.yourcompany.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private ProductRequest productRequest;
    private ProductResponse productResponse1;

    @BeforeEach
    void setUp() {
        // Arrange - Test data setup
        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(BigDecimal.valueOf(100.0));

        product1 = new Product("1", "Test Product 1", "Description 1", 100.0);
        product2 = new Product("2", "Test Product 2", "Description 2", 200.0);

        productResponse1 = new ProductResponse();
        productResponse1.setId("1");
        productResponse1.setName("Test Product 1");
        productResponse1.setDescription("Description 1");
        productResponse1.setPrice(BigDecimal.valueOf(100.0));
    }

    @Test
    void createProduct_shouldReturnProductResponse() {
        // Arrange
        when(productMapper.toProduct(any(ProductRequest.class))).thenReturn(product1);
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product1));
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse1);

        // Act
        Mono<ProductResponse> result = productService.createProduct(productRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    return response.getId().equals("1") && response.getName().equals("Test Product 1");
                })
                .verifyComplete();
    }

    @Test
    void getAllProducts_shouldReturnFluxOfProductResponses() {
        // Arrange
        List<Product> productList = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(Flux.fromIterable(productList));
        when(productMapper.toProductResponse(product1)).thenReturn(productResponse1);
        // Assume productResponse2 would be created for product2
        ProductResponse productResponse2 = new ProductResponse();
        productResponse2.setId("2");
        when(productMapper.toProductResponse(product2)).thenReturn(productResponse2);


        // Act
        Flux<ProductResponse> result = productService.getAllProducts();

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProductResponse() {
        // Arrange
        when(productRepository.findById("1")).thenReturn(Mono.just(product1));
        when(productMapper.toProductResponse(product1)).thenReturn(productResponse1);

        // Act
        Mono<ProductResponse> result = productService.getProductById("1");

        // Assert
        StepVerifier.create(result)
                .expectNext(productResponse1)
                .verifyComplete();
    }

    @Test
    void getProductById_whenProductDoesNotExist_shouldReturnEmptyMono() {
        // Arrange
        when(productRepository.findById("99")).thenReturn(Mono.empty());

        // Act
        Mono<ProductResponse> result = productService.getProductById("99");

        // Assert
        StepVerifier.create(result)
                .verifyComplete(); // Expecting no items, just completion
    }


    @Test
    void updateProduct_shouldReturnUpdatedProductResponse() {
        // Arrange
        Product updatedProduct = new Product("1", "Updated Name", "Updated Desc", 150.0);
        ProductResponse updatedResponse = new ProductResponse();
        updatedResponse.setId("1");
        updatedResponse.setName("Updated Name");

        when(productRepository.findById("1")).thenReturn(Mono.just(product1));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));
        when(productMapper.toProductResponse(updatedProduct)).thenReturn(updatedResponse);

        // Act
        Mono<ProductResponse> result = productService.updateProduct("1", productRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getName().equals("Updated Name"))
                .verifyComplete();
    }

    @Test
    void deleteProduct_shouldCompleteSuccessfully() {
        // Arrange
        when(productRepository.deleteById("1")).thenReturn(Mono.empty()); // deleteById returns Mono<Void>

        // Act
        Mono<Void> result = productService.deleteProduct("1");

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }
}
