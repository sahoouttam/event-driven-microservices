package com.event.driven.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.stock.service.dto.request.CreateProductRequest;
import com.event.driven.stock.service.dto.response.ProductResponse;
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
}
