package com.event.driven.fulfillment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.event.driven.common.service.events.OrderConfirmedEvent;
import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.fulfillment.service.client.AccountClient;
import com.event.driven.fulfillment.service.dto.response.AddressResponse;
import com.event.driven.fulfillment.service.dto.response.CustomerResponse;
import com.event.driven.fulfillment.service.dto.response.FulfillmentResponse;
import com.event.driven.fulfillment.service.entity.Fulfillment;
import com.event.driven.fulfillment.service.enums.FulfillmentStatus;
import com.event.driven.fulfillment.service.repository.FulfillmentItemRepository;
import com.event.driven.fulfillment.service.repository.FulfillmentRepository;
import com.event.driven.fulfillment.service.service.FulfillmentService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class FulfillmentServiceTest {
    
    @Autowired
    private FulfillmentService fulfillmentService;

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private FulfillmentItemRepository fulfillmentItemRepository;

    @MockitoBean
    private AccountClient accountClient;

    @BeforeEach
    void setup() {
        fulfillmentRepository.deleteAll();
        fulfillmentItemRepository.deleteAll();
    }

    @Test
    void shouldCreateFulfillment() {
        List<OrderItemEvent> orderItemEvents = Arrays.asList(
            new OrderItemEvent(4L, "SKU-001", "PRODUCT-123", 2)
        );
        OrderConfirmedEvent orderConfirmedEvent = OrderConfirmedEvent.builder()
                                .orderId(1L)
                                .customerId(2L)
                                .shippingAddressId(3L)
                                .totalAmount(BigDecimal.valueOf(100))
                                .orderItemEvents(orderItemEvents)
                                .build();

        CustomerResponse customerResponse = CustomerResponse.builder()
                                .id(2L)
                                .name("John Doe")
                                .email("john@email.com")
                                .phone("0987654321")
                                .build();
        AddressResponse addressResponse = AddressResponse.builder()
                                .addressId(3L)
                                .street("123 main street")
                                .city("NYC")
                                .state("NY")
                                .build();

        when(accountClient.getCustomer(2L)).thenReturn(
                CompletableFuture.completedFuture(customerResponse));
        when(accountClient.getAddress(2L, 3L)).thenReturn(
                CompletableFuture.completedFuture(addressResponse));

        fulfillmentService.createFulfillment(orderConfirmedEvent);

        List<Fulfillment> fulfillments = fulfillmentRepository.findAll();
        assertTrue(fulfillments.size() >= 1);

        Fulfillment fulfillment = fulfillments.get(0);
        assertNotNull(fulfillment.getId());
        assertEquals(FulfillmentStatus.PENDING, fulfillment.getFulfillmentStatus());
        assertEquals("John Doe", fulfillment.getCustomerName());
        assertEquals("123 main street", fulfillment.getShippingStreet());
    }

    @Test
    void shouldShipAndDeliverOrder() {
        Fulfillment fulfillment = Fulfillment.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .fulfillmentStatus(FulfillmentStatus.PACKED)
                                    .build();
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);

        FulfillmentResponse fulfillmentShipped = fulfillmentService.shipOrder(savedFulfillment.getId());
        assertEquals(FulfillmentStatus.SHIPPED, fulfillmentShipped.getFulfillmentStatus());
        assertNotNull(fulfillmentShipped.getCarrier());
        assertNotNull(fulfillmentShipped.getTrackingNumber());

        fulfillmentService.markDelivered(savedFulfillment.getId());

        FulfillmentResponse fulfillmentResponse = fulfillmentService.getFulfillment(savedFulfillment.getId());
        assertEquals(FulfillmentStatus.DELIVERED, fulfillmentResponse.getFulfillmentStatus());
        assertNotNull(fulfillmentResponse.getDeliveredAt());
    }

    @Test
    void shouldCancelFulfillment() {
        Fulfillment fulfillment = Fulfillment.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .fulfillmentStatus(FulfillmentStatus.PENDING)
                                    .build();
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);

        fulfillmentService.cancelFulfillment(savedFulfillment.getId());

        FulfillmentResponse fulfillmentResponse = fulfillmentService.getFulfillment(savedFulfillment.getId());
        assertEquals(FulfillmentStatus.CANCELLED, fulfillmentResponse.getFulfillmentStatus());
    }

    @Test
    void shouldFindFulfillmentByOrderId() {
        Fulfillment fulfillment = Fulfillment.builder()
                                    .orderId(100L)
                                    .customerId(99L)
                                    .fulfillmentStatus(FulfillmentStatus.SHIPPED)
                                    .build();

        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);

        FulfillmentResponse fulfillmentResponse = fulfillmentService.getFulfillment(savedFulfillment.getId());
        assertEquals(savedFulfillment.getId(), fulfillmentResponse.getId());
    }
}
