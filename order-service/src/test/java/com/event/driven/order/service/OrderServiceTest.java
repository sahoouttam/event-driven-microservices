package com.event.driven.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.event.driven.order.service.client.StockClient;
import com.event.driven.order.service.dto.request.CreateOrderRequest;
import com.event.driven.order.service.dto.request.OrderItemRequest;
import com.event.driven.order.service.dto.response.OrderResponse;
import com.event.driven.order.service.dto.response.ProductResponse;
import com.event.driven.order.service.enums.OrderStatus;
import com.event.driven.order.service.service.OrderService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private StockClient stockClient;

    @Test
    void shouldCreateOrder() {
        ProductResponse productResponse1 = ProductResponse.builder()
                                .id(10L)
                                .sku("SKU-001")
                                .name("Product 1")
                                .price(BigDecimal.valueOf(50))
                                .active(true)
                                .build();
        
        ProductResponse productResponse2 = ProductResponse.builder()
                                .id(11L)
                                .sku("SKU-002")
                                .name("Product 2")
                                .price(BigDecimal.valueOf(70))
                                .active(true)
                                .build();

        when(stockClient.getProductBySku("SKU-001")).thenReturn(productResponse1);
        when(stockClient.getProductBySku("SKU-002")).thenReturn(productResponse2);

        List<OrderItemRequest> orderItemRequests = Arrays.asList(
            new OrderItemRequest("SKU-001", 1),
            new OrderItemRequest("SKU-002", 2)
        );
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                                .customerId(1L)
                                .ShippingAddressId(2L)
                                .paymentMethod("CREDIT_CARD")
                                .orderItemRequests(orderItemRequests)
                                .build();

        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);

        assertNotNull(orderResponse.getId());
        assertEquals(orderResponse.getTotalAmount(), BigDecimal.valueOf(190));
        assertEquals(OrderStatus.PENDING_INVENTORY, orderResponse.getOrderStatus());
    }

    @Test
    void shouldGetOrderById() {
        ProductResponse productResponse = ProductResponse.builder()
                                .id(10L)
                                .sku("SKU-002")
                                .name("Product 1")
                                .price(BigDecimal.valueOf(49.9))
                                .active(true)
                                .build();
        
        when(stockClient.getProductBySku("SKU-002")).thenReturn(productResponse);
        List<OrderItemRequest> orderItemRequests = Arrays.asList(
            new OrderItemRequest("SKU-002", 2)
        );
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                                .customerId(1L)
                                .ShippingAddressId(2L)
                                .paymentMethod("CREDIT_CARD")
                                .orderItemRequests(orderItemRequests)
                                .build();

        OrderResponse orderCreated = orderService.createOrder(createOrderRequest);

        OrderResponse orderFound = orderService.getOrder(orderCreated.getId());

        assertEquals(orderCreated.getId(), orderFound.getId());
        assertEquals(orderCreated.getOrderNumber(), orderFound.getOrderNumber());
        assertEquals(0, BigDecimal.valueOf(99.8).compareTo(orderFound.getTotalAmount()));
    }

    @Test
    void shouldUpdateOrderStatusToConfirmed() {
        ProductResponse productResponse1 = ProductResponse.builder()
                                .id(10L)
                                .sku("SKU-001")
                                .name("Product 1")
                                .price(BigDecimal.valueOf(50))
                                .active(true)
                                .build();
        
        ProductResponse productResponse2 = ProductResponse.builder()
                                .id(11L)
                                .sku("SKU-002")
                                .name("Product 2")
                                .price(BigDecimal.valueOf(70))
                                .active(true)
                                .build();

        when(stockClient.getProductBySku("SKU-001")).thenReturn(productResponse1);
        when(stockClient.getProductBySku("SKU-002")).thenReturn(productResponse2);

        List<OrderItemRequest> orderItemRequests = Arrays.asList(
            new OrderItemRequest("SKU-001", 1),
            new OrderItemRequest("SKU-002", 2)
        );
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                                .customerId(1L)
                                .ShippingAddressId(2L)
                                .paymentMethod("CREDIT_CARD")
                                .orderItemRequests(orderItemRequests)
                                .build();

        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);
        assertEquals(orderResponse.getOrderStatus(), OrderStatus.PENDING_INVENTORY);

        orderService.updateStatus(orderResponse.getId(), OrderStatus.CONFIRMED);
        OrderResponse orderFound = orderService.getOrder(orderResponse.getId());
        assertEquals(orderFound.getOrderStatus(), OrderStatus.CONFIRMED);
    }

    @Test
    void shouldUpdateOrderStatusToFailed() {
        ProductResponse productResponse = ProductResponse.builder()
                                .id(10L)
                                .sku("SKU-001")
                                .name("Product 1")
                                .price(BigDecimal.valueOf(50))
                                .active(true)
                                .build();
        

        when(stockClient.getProductBySku("SKU-001")).thenReturn(productResponse);
       List<OrderItemRequest> orderItemRequests = Arrays.asList(
            new OrderItemRequest("SKU-001", 1)
        );
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                                .customerId(1L)
                                .ShippingAddressId(2L)
                                .paymentMethod("CREDIT_CARD")
                                .orderItemRequests(orderItemRequests)
                                .build();

        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);
        assertEquals(orderResponse.getOrderStatus(), OrderStatus.PENDING_INVENTORY);

        orderService.updateStatus(orderResponse.getId(), OrderStatus.FAILED);
        OrderResponse orderFound = orderService.getOrder(orderResponse.getId());
        assertEquals(orderFound.getOrderStatus(), OrderStatus.FAILED);
    }

    @Test
    void shouldGetAllCustomerOrders() {
        ProductResponse productResponse1 = ProductResponse.builder()
                               
        .id(10L)
                                .sku("SKU-001")
                                .name("Product 1")
                                .price(BigDecimal.valueOf(50))
                                .active(true)
                                .build();
        

        when(stockClient.getProductBySku("SKU-001")).thenReturn(productResponse1);
       List<OrderItemRequest> orderItemRequests1 = Arrays.asList(
            new OrderItemRequest("SKU-001", 1)
        );
        CreateOrderRequest createOrderRequest1 = CreateOrderRequest.builder()
                                .customerId(1L)
                                .ShippingAddressId(2L)
                                .paymentMethod("CREDIT_CARD")
                                .orderItemRequests(orderItemRequests1)
                                .build();
        OrderResponse orderResponse1 = orderService.createOrder(createOrderRequest1);
        assertEquals(1L, orderResponse1.getCustomerId());
        assertEquals(1, orderResponse1.getOrderItems().size());

        ProductResponse productResponse2 = ProductResponse.builder()
                                .id(11L)
                                .sku("SKU-002")
                                .name("Product 2")
                                .price(BigDecimal.valueOf(60))
                                .active(true)
                                .build();

        ProductResponse productResponse3 = ProductResponse.builder()
                                .id(12L)
                                .sku("SKU-002")
                                .name("Product 3")
                                .price(BigDecimal.valueOf(70))
                                .active(true)
                                .build();
        

        when(stockClient.getProductBySku("SKU-002")).thenReturn(productResponse2);
        when(stockClient.getProductBySku("SKU-003")).thenReturn(productResponse3);
        List<OrderItemRequest> orderItemRequests2 = Arrays.asList(
            new OrderItemRequest("SKU-002", 2),
            new OrderItemRequest("SKU-003", 3)
        );
        CreateOrderRequest createOrderRequest2 = CreateOrderRequest.builder()
                                .customerId(1L)
                                .ShippingAddressId(2L)
                                .paymentMethod("DEBIT_CARD")
                                .orderItemRequests(orderItemRequests2)
                                .build();
        OrderResponse orderResponse2 = orderService.createOrder(createOrderRequest2);
        assertEquals(1L, orderResponse2.getCustomerId());
        assertEquals(2, orderResponse2.getOrderItems().size());

        List<OrderResponse> orderResponses = orderService.getAllOrders(1L);
        assertEquals(2, orderResponses.size());
    }
}
