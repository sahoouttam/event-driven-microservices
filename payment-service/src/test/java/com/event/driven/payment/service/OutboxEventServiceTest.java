package com.event.driven.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.common.service.events.PaymentCompletedEvent;
import com.event.driven.payment.service.entity.OutboxEvent;
import com.event.driven.payment.service.enums.EventType;
import com.event.driven.payment.service.repository.OutboxEventRepository;
import com.event.driven.payment.service.service.OutboxEventService;

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
        PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
                                    .paymentId(1L)
                                    .orderId(2L)
                                    .transactionId("TXN-123")
                                    .build();

        outboxEventService.saveEvent(EventType.PAYMENT_COMPLETED, 
                            AggregateType.PAYMENT, 
                            "1", 
                            paymentCompletedEvent);

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertEquals(1, outboxEvents.size());

        OutboxEvent outboxEvent = outboxEvents.get(0);
        assertEquals(EventStatus.PENDING, outboxEvent.getEventStatus());
        assertEquals(EventType.PAYMENT_COMPLETED, outboxEvent.getEventType());
    }

    @Test
    void shouldMarkAsPublished() {
        PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
                                    .paymentId(1L)
                                    .orderId(2L)
                                    .transactionId("TXN-123")
                                    .build();

        outboxEventService.saveEvent(EventType.PAYMENT_COMPLETED, 
                            AggregateType.PAYMENT, 
                            "1", 
                            paymentCompletedEvent);

        OutboxEvent savedEvent = outboxEventRepository.findAll().get(0);
        outboxEventService.markUpdate(savedEvent, EventStatus.PUBLISHED);

        OutboxEvent updatedEvent = outboxEventRepository.findById(savedEvent.getId())
                                        .orElseThrow();
        assertEquals(EventStatus.PUBLISHED, updatedEvent.getEventStatus());
    }

    @Test
    void shouldIncrementRetryCount() {
        PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
                                    .paymentId(1L)
                                    .orderId(2L)
                                    .transactionId("TXN-123")
                                    .build();

        outboxEventService.saveEvent(EventType.PAYMENT_COMPLETED, 
                            AggregateType.PAYMENT, 
                            "1", 
                            paymentCompletedEvent);

        OutboxEvent savedEvent = outboxEventRepository.findAll().get(0);
        outboxEventService.incrementRetryCount(savedEvent);

        OutboxEvent updatedEvent = outboxEventRepository.findById(savedEvent.getId())
                                        .orElseThrow();
        assertEquals(1, updatedEvent.getRetryCount());
    }
}
