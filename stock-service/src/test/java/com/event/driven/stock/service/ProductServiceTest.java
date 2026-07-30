package com.event.driven.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.stock.service.dto.request.CreateProductRequest;
import com.event.driven.stock.service.dto.request.UpdateProductRequest;
import com.event.driven.stock.service.dto.response.ProductResponse;
import com.event.driven.stock.service.exception.ResourceNotFoundException;
import com.event.driven.stock.service.service.ProductService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ProductServiceTest {
    
    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-100")
                                   .name("Test Product")
                                   .price(BigDecimal.valueOf(29.99))
                                   .build(); 
        ProductResponse productResponse = productService.createProduct(createProductRequest);

        assertNotNull(productResponse.getId());
        assertEquals("SKU-100", productResponse.getSku());
        assertEquals("Test Product", productResponse.getName());
        assertTrue(productResponse.isActive());
    }

    @Test
    void shouldFindProductById() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-101")
                                   .name("New Product")
                                   .price(BigDecimal.valueOf(999.99))
                                   .build(); 
        ProductResponse productCreated = productService.createProduct(createProductRequest);

        ProductResponse productFound = productService.findProductBySku("SKU-101");
        assertEquals(productCreated.getId(), productFound.getId());
        assertEquals("New Product", productFound.getName());
        assertEquals(BigDecimal.valueOf(999.99), productFound.getPrice());   
    }

    @Test
    void shouldUpdateProduct() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-12345")
                                   .name("Name Product")
                                   .price(BigDecimal.valueOf(99.99))
                                   .build(); 
        ProductResponse productCreated = productService.createProduct(createProductRequest);

        UpdateProductRequest updateProductRequest = UpdateProductRequest.builder()
                                    .name("New Name Product")
                                    .price(BigDecimal.valueOf(199.49))
                                    .active(false)
                                    .build();
        ProductResponse productUpdated = productService.updateProduct(
                                        productCreated.getId(), updateProductRequest);
        assertEquals("New Name Product", productUpdated.getName());
        assertEquals(BigDecimal.valueOf(199.49), productUpdated.getPrice()); 
    }

    @Test
    void shouldFindAllProduct() {
        CreateProductRequest createProductRequest1 = CreateProductRequest.builder()
                                   .sku("SKU-12345")
                                   .name("Product 12345")
                                   .price(BigDecimal.valueOf(199.99))
                                   .build(); 
        productService.createProduct(createProductRequest1);
        CreateProductRequest createProductRequest2 = CreateProductRequest.builder()
                                   .sku("SKU-123456")
                                   .name("Product 123456")
                                   .price(BigDecimal.valueOf(299.99))
                                   .build(); 
        productService.createProduct(createProductRequest2);

        List<ProductResponse> productResponses = productService.findAllProducts();
        assertTrue(productResponses.size() >= 2);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.findBySku("SKU-ABC-123");
        });
    }
}
