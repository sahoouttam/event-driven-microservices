package com.event.driven.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.common.service.events.StockReservationCreatedEvent;
import com.event.driven.stock.service.entity.OutboxEvent;
import com.event.driven.stock.service.enums.EventType;
import com.event.driven.stock.service.repository.OutboxEventRepository;
import com.event.driven.stock.service.service.OutboxEventService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class OutboxEventServiceTest {
    
    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void shouldSaveEvent() {
        StockReservationCreatedEvent stockReservationCreatedEvent = 
                        StockReservationCreatedEvent.builder()
                                .productId(1L)
                                .sku("SKU-123")
                                .quantity(100)
                                .availableQuantity(250)
                                .orderId(99L)
                                .build();
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "2", 
                                stockReservationCreatedEvent);

        List<OutboxEvent> outboxEvents = outboxEventService.findAllOutboxEvent();
        assertEquals(1, outboxEvents.size());

        OutboxEvent outboxEvent = outboxEvents.get(0);
        assertEquals(EventType.STOCK_RESERVATION_CREATED, outboxEvent.getEventType());
        assertEquals(AggregateType.INVENTORY, outboxEvent.getAggregateType());
        assertEquals("2", outboxEvent.getAggregateId());
        assertEquals(EventStatus.PENDING, outboxEvent.getEventStatus());
        assertEquals(0, outboxEvent.getRetryCount());
        assertNotNull(outboxEvent.getId());
        assertTrue(outboxEvent.getPayload().contains("SKU-123"));
    }

    @Test
    void shouldFindPendingEvents() {
        StockReservationCreatedEvent stockReservationCreatedEvent = 
                        StockReservationCreatedEvent.builder()
                                .productId(2L)
                                .sku("SKU-1234")
                                .quantity(10)
                                .availableQuantity(50)
                                .orderId(100L)
                                .build();
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "1", 
                                stockReservationCreatedEvent);
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "2", 
                                stockReservationCreatedEvent);

        List<OutboxEvent> outboxEvents = outboxEventService
                        .findByEventStatus(EventStatus.PENDING, 5);
        assertEquals(2, outboxEvents.size());
    }

    @Test
    void shouldMarkEventAsPublished() {
        StockReservationCreatedEvent stockReservationCreatedEvent = 
                        StockReservationCreatedEvent.builder()
                                .productId(3L)
                                .sku("SKU-12345")
                                .quantity(1000)
                                .availableQuantity(1000)
                                .orderId(101L)
                                .build();
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "199", 
                                stockReservationCreatedEvent);
        
        OutboxEvent outboxEvent = outboxEventService.findAllOutboxEvent().get(0);
        assertEquals(EventStatus.PENDING, outboxEvent.getEventStatus());

        outboxEventService.markUpdate(outboxEvent, EventStatus.PUBLISHED);
        OutboxEvent updatedOutboxEvent = outboxEventService.findById(outboxEvent.getId());
        assertEquals(EventStatus.PUBLISHED, updatedOutboxEvent.getEventStatus());
    }

    @Test
    void shouldMarkEventAsFailed() {
        StockReservationCreatedEvent stockReservationCreatedEvent = 
                        StockReservationCreatedEvent.builder()
                                .productId(3L)
                                .sku("SKU-123456")
                                .quantity(500)
                                .availableQuantity(500)
                                .orderId(102L)
                                .build();
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "299", 
                                stockReservationCreatedEvent);
        
        OutboxEvent outboxEvent = outboxEventService.findAllOutboxEvent().get(0);
        assertEquals(EventStatus.PENDING, outboxEvent.getEventStatus());

        outboxEventService.markUpdate(outboxEvent, EventStatus.FAILED);
        OutboxEvent updatedOutboxEvent = outboxEventService.findById(outboxEvent.getId());
        assertEquals(EventStatus.FAILED, updatedOutboxEvent.getEventStatus());
    }

    @Test
    void shouldIncrementRetryCount() {
        StockReservationCreatedEvent stockReservationCreatedEvent = 
                        StockReservationCreatedEvent.builder()
                                .productId(3L)
                                .sku("SKU-1234567")
                                .quantity(2000)
                                .availableQuantity(1000)
                                .orderId(123L)
                                .build();
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "99", 
                                stockReservationCreatedEvent);
        
        OutboxEvent outboxEvent = outboxEventService.findAllOutboxEvent().get(0);
        assertEquals(0, outboxEvent.getRetryCount());

        outboxEventService.incrementRetryCount(outboxEvent);
        assertEquals(1, outboxEvent.getRetryCount());
    }

    @Test
    void shouldFilterByRetryCount() {
        StockReservationCreatedEvent stockReservationCreatedEvent = 
                        StockReservationCreatedEvent.builder()
                                .productId(3L)
                                .sku("SKU-12")
                                .quantity(250)
                                .availableQuantity(200)
                                .orderId(1234L)
                                .build();
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED, 
                                AggregateType.INVENTORY, 
                                "999", 
                                stockReservationCreatedEvent);
        
        OutboxEvent outboxEvent = outboxEventService.findAllOutboxEvent().get(0);
        for (int index = 0; index < 5; index++) {
            outboxEventService.incrementRetryCount(outboxEvent);
        }
        List<OutboxEvent> outboxEvents = outboxEventService
                    .findByEventStatus(EventStatus.PENDING, 4);
        assertEquals(0, outboxEvents.size());
    }
}
