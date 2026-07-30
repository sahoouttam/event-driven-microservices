package com.event.driven.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.stock.service.dto.request.AddStockRequest;
import com.event.driven.stock.service.dto.request.CreateProductRequest;
import com.event.driven.stock.service.dto.request.ReserveStockRequest;
import com.event.driven.stock.service.dto.response.InventoryResponse;
import com.event.driven.stock.service.service.InventoryService;
import com.event.driven.stock.service.service.ProductService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class InventoryServiceTest {
    
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldAddStock() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-100")
                                   .name("Test Product")
                                   .price(BigDecimal.valueOf(29.99))
                                   .build(); 
        productService.createProduct(createProductRequest);

        inventoryService.createInventory("SKU-100");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-100")
                                    .quantity(100)
                                    .build();
        InventoryResponse inventoryResponse = inventoryService.addStock(addStockRequest);
        assertEquals(100, inventoryResponse.getAvailableQuantity());
        assertEquals("Test Product", inventoryResponse.getProductName());
    }

    @Test
    void shouldReserveStock() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-101")
                                   .name("Test Product 1")
                                   .price(BigDecimal.valueOf(129.99))
                                   .build(); 
        productService.createProduct(createProductRequest);
        inventoryService.createInventory("SKU-101");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-101")
                                    .quantity(100)
                                    .build();
        inventoryService.addStock(addStockRequest);

        ReserveStockRequest reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-101")
                                    .quantity(40)
                                    .orderId(1L)
                                    .build();
        inventoryService.createReservation(reserveStockRequest);
        InventoryResponse inventoryResponse = inventoryService.confirmReservation(reserveStockRequest);
        assertEquals(60, inventoryResponse.getTotalQuantity());
        assertEquals(60, inventoryResponse.getAvailableQuantity());
    }

    @Test
    void shouldFailReservationWhenInsufficientStock() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-102")
                                   .name("Test Product 2")
                                   .price(BigDecimal.valueOf(229.99))
                                   .build(); 
        productService.createProduct(createProductRequest);
        inventoryService.createInventory("SKU-102");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-102")
                                    .quantity(100)
                                    .build();
        inventoryService.addStock(addStockRequest);

        ReserveStockRequest reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-102")
                                    .quantity(200)
                                    .orderId(1L)
                                    .build();
        InventoryResponse inventoryResponse = inventoryService.createReservation(reserveStockRequest);
        assertEquals(100, inventoryResponse.getTotalQuantity());
        assertEquals(100, inventoryResponse.getAvailableQuantity());
    }

    @Test
    void shouldConfirmReservation() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-103")
                                   .name("Test Product 3")
                                   .price(BigDecimal.valueOf(229.99))
                                   .build(); 
        productService.createProduct(createProductRequest);
        inventoryService.createInventory("SKU-103");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-103")
                                    .quantity(1000)
                                    .build();
        inventoryService.addStock(addStockRequest);

        ReserveStockRequest reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-103")
                                    .quantity(200)
                                    .orderId(2L)
                                    .build();
        inventoryService.createReservation(reserveStockRequest);

        InventoryResponse inventoryResponse = inventoryService.confirmReservation(reserveStockRequest);
        assertEquals(800, inventoryResponse.getTotalQuantity());
        assertEquals(800, inventoryResponse.getAvailableQuantity());
    }

    @Test
    void shouldReleaseReservation() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-104")
                                   .name("Test Product 4")
                                   .price(BigDecimal.valueOf(999.99))
                                   .build(); 
        productService.createProduct(createProductRequest);
        inventoryService.createInventory("SKU-104");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-104")
                                    .quantity(100)
                                    .build();
        inventoryService.addStock(addStockRequest);

        ReserveStockRequest reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-104")
                                    .quantity(20)
                                    .orderId(2L)
                                    .build();
        inventoryService.createReservation(reserveStockRequest);

        InventoryResponse inventoryResponse = inventoryService.releaseReservation(reserveStockRequest);
        assertEquals(100, inventoryResponse.getTotalQuantity());
        assertEquals(100, inventoryResponse.getAvailableQuantity());
    }

    @Test
    void shouldRestoreStock() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-105")
                                   .name("Test Product 5")
                                   .price(BigDecimal.valueOf(500.00))
                                   .build(); 
        productService.createProduct(createProductRequest);
        inventoryService.createInventory("SKU-105");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-105")
                                    .quantity(50)
                                    .build();
        inventoryService.addStock(addStockRequest);

        ReserveStockRequest reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-105")
                                    .quantity(20)
                                    .orderId(2L)
                                    .build();
        inventoryService.confirmReservation(reserveStockRequest);

        AddStockRequest restoreRequest = AddStockRequest.builder()
                                    .sku("SKU-105")
                                    .quantity(20)
                                    .build();
        inventoryService.restoreStock(restoreRequest);

        InventoryResponse inventoryResponse = inventoryService.getInventory("SKU-105");
        assertEquals(50, inventoryResponse.getTotalQuantity());
        assertEquals(50, inventoryResponse.getAvailableQuantity());
    }

    @Test
    void shouldGetInventory() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-1234")
                                   .name("Test Product 1234")
                                   .price(BigDecimal.valueOf(899.49))
                                   .build(); 
        productService.createProduct(createProductRequest);

        inventoryService.createInventory("SKU-1234");
        AddStockRequest addStockRequest1 = AddStockRequest.builder()
                                    .sku("SKU-1234")
                                    .quantity(5000)
                                    .build();
        inventoryService.addStock(addStockRequest1);
        
        InventoryResponse inventoryResponse = inventoryService.getInventory("SKU-1234");
        assertNotNull(inventoryResponse.getId());
        assertEquals("SKU-1234", inventoryResponse.getSku());
        assertEquals("Test Product 1234", inventoryResponse.getProductName());
        assertEquals(5000, inventoryResponse.getTotalQuantity());
        assertEquals(5000, inventoryResponse.getAvailableQuantity());
    }
    
    @Test
    void shouldAddMultipleStocks() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-12345")
                                   .name("Test Product 12345")
                                   .price(BigDecimal.valueOf(1000.00))
                                   .build(); 
        productService.createProduct(createProductRequest);

        inventoryService.createInventory("SKU-12345");
        AddStockRequest addStockRequest1 = AddStockRequest.builder()
                                    .sku("SKU-12345")
                                    .quantity(500)
                                    .build();
        InventoryResponse inventoryResponse1 = inventoryService.addStock(addStockRequest1);
        assertEquals(500, inventoryResponse1.getAvailableQuantity());
        assertEquals(500, inventoryResponse1.getTotalQuantity());

        AddStockRequest addStockRequest2 = AddStockRequest.builder()
                                    .sku("SKU-12345")
                                    .quantity(1000)
                                    .build();
        InventoryResponse inventoryResponse2 = inventoryService.addStock(addStockRequest2);
        assertEquals(1500, inventoryResponse2.getAvailableQuantity());
        assertEquals(1500, inventoryResponse2.getTotalQuantity());
    }

    @Test
    void shouldHandleFullFlow() {
        CreateProductRequest createProductRequest = CreateProductRequest.builder()
                                   .sku("SKU-123456")
                                   .name("Test Product 123456")
                                   .price(BigDecimal.valueOf(999.99))
                                   .build(); 
        productService.createProduct(createProductRequest);
        inventoryService.createInventory("SKU-123456");
        AddStockRequest addStockRequest = AddStockRequest.builder()
                                    .sku("SKU-123456")
                                    .quantity(1000)
                                    .build();
        inventoryService.addStock(addStockRequest);

        ReserveStockRequest reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-123456")
                                    .quantity(250)
                                    .orderId(1L)
                                    .build();
        InventoryResponse responseCreated = inventoryService.createReservation(reserveStockRequest);
        assertEquals(1000, responseCreated.getTotalQuantity());
        assertEquals(750, responseCreated.getAvailableQuantity());

        InventoryResponse responseConfirmed = inventoryService.confirmReservation(reserveStockRequest);
        assertEquals(750, responseConfirmed.getTotalQuantity());
        assertEquals(750, responseConfirmed.getAvailableQuantity());

        reserveStockRequest = ReserveStockRequest.builder()
                                    .sku("SKU-123456")
                                    .quantity(500)
                                    .orderId(1L)
                                    .build();
        responseCreated = inventoryService.createReservation(reserveStockRequest);
        assertEquals(750, responseCreated.getTotalQuantity());
        assertEquals(250, responseCreated.getAvailableQuantity());

        InventoryResponse responseReleased = inventoryService.releaseReservation(reserveStockRequest);
        assertEquals(750, responseReleased.getTotalQuantity());
        assertEquals(750, responseReleased.getAvailableQuantity());
    }
}
