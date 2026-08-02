package com.event.driven.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.common.service.events.OrderCancelledEvent;
import com.event.driven.common.service.events.OrderConfirmedEvent;
import com.event.driven.common.service.events.OrderCreatedEvent;
import com.event.driven.order.service.entity.OutboxEvent;
import com.event.driven.order.service.enums.EventType;
import com.event.driven.order.service.repository.OutboxEventRepository;
import com.event.driven.order.service.service.OutboxEventService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class OutboxEventServiceTest {
    
    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setup() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void shouldSaveEvent() {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .orderNumber("ORD-1")
                                    .build();
        
        outboxEventService.saveEvent(EventType.ORDER_CREATED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderCreatedEvent); 
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertEquals(1, outboxEvents.size());
        
        OutboxEvent outboxEvent = outboxEvents.get(0);
        assertEquals(EventType.ORDER_CREATED, outboxEvent.getEventType());
        assertEquals(EventStatus.PENDING, outboxEvent.getEventStatus());
    }

    @Test
    void shouldMarkAsPublished() {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .orderNumber("ORD-1")
                                    .build();
        
        outboxEventService.saveEvent(EventType.ORDER_CREATED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderCreatedEvent); 
        
        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEventService.markUpdate(outboxEvent, EventStatus.PUBLISHED);

        OutboxEvent updatedEvent = outboxEventRepository
                                        .findById(outboxEvent.getId())
                                        .orElseThrow();
        assertEquals(EventStatus.PUBLISHED, updatedEvent.getEventStatus());
    }

    @Test
    void shouldMarkAsFailed() {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .orderNumber("ORD-1")
                                    .build();
        
        outboxEventService.saveEvent(EventType.ORDER_CREATED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderCreatedEvent); 
        
        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEventService.markUpdate(outboxEvent, EventStatus.FAILED);

        OutboxEvent updatedEvent = outboxEventRepository
                                        .findById(outboxEvent.getId())
                                        .orElseThrow();
        assertEquals(EventStatus.FAILED, updatedEvent.getEventStatus());
    }

    @Test
    void shouldIncrementRetryCount() {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .orderNumber("ORD-1")
                                    .build();
        
        outboxEventService.saveEvent(EventType.ORDER_CREATED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderCreatedEvent); 
        
        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEventService.incrementRetryCount(outboxEvent);

        OutboxEvent updatedEvent = outboxEventRepository
                                        .findById(outboxEvent.getId())
                                        .orElseThrow();
        assertEquals(1, updatedEvent.getRetryCount());
    }

    @Test
    void shouldFindPendingEvents() {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .orderNumber("ORD-1")
                                    .build();

        OrderConfirmedEvent orderConfirmedEvent = OrderConfirmedEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .shippingAddressId(3L)
                                    .totalAmount(BigDecimal.valueOf(250.99))
                                    .build();
        
        OrderCancelledEvent orderCancelledEvent = OrderCancelledEvent.builder()
                                    .orderId(1L)
                                    .customerId(2L)
                                    .build();
        
        outboxEventService.saveEvent(EventType.ORDER_CREATED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderCreatedEvent); 
        outboxEventService.saveEvent(EventType.ORDER_CONFIRMED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderConfirmedEvent); 
        outboxEventService.saveEvent(EventType.ORDER_CANCELLED, 
                                    AggregateType.ORDER,
                                    "1", 
                                    orderCancelledEvent); 
        
        List<OutboxEvent> outboxEvents = outboxEventService.findByEventStatus(EventStatus.PENDING, 5);
        assertEquals(3, outboxEvents.size());
    }
}
