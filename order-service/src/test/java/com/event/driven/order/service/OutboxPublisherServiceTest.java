package com.event.driven.order.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.order.service.entity.OutboxEvent;
import com.event.driven.order.service.kafka.OrderKafkaPublisher;
import com.event.driven.order.service.service.OutboxEventService;
import com.event.driven.order.service.service.OutboxPublisherService;

@ExtendWith(MockitoExtension.class)
public class OutboxPublisherServiceTest {
    
    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private OrderKafkaPublisher orderKafkaPublisher;

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;

    @Test
    void shouldPublishPendingEvents() {
        OutboxEvent outboxEvent1 = OutboxEvent.builder()
                        .eventId("event-1")
                        .eventStatus(EventStatus.PENDING)
                        .retryCount(0)
                        .build();
        OutboxEvent outboxEvent2 = OutboxEvent.builder()
                        .eventId("event-2")
                        .eventStatus(EventStatus.PENDING)
                        .retryCount(0)
                        .build();

        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                        .thenReturn(Arrays.asList(outboxEvent1, outboxEvent2));

        outboxPublisherService.publishPendingEvents();

        verify(orderKafkaPublisher, times(2)).publish(any(OutboxEvent.class));
        verify(outboxEventService).markUpdate(outboxEvent1, EventStatus.PUBLISHED);
        verify(outboxEventService).markUpdate(outboxEvent2, EventStatus.PUBLISHED);
    }

    @Test
    void shouldIncrementRetryOnFailure() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-1")
                        .eventStatus(EventStatus.PENDING)
                        .retryCount(0)
                        .build();

        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                        .thenReturn(Arrays.asList(outboxEvent));
        doThrow(new RuntimeException("Error")).when(orderKafkaPublisher)
                        .publish(outboxEvent);

        outboxPublisherService.publishPendingEvents();

        verify(outboxEventService).incrementRetryCount(outboxEvent);
        verify(outboxEventService, never()).markUpdate(outboxEvent, EventStatus.PUBLISHED);
    }

    @Test
    void shouldMarkFailedAfterMaxRetries() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-1")
                        .eventStatus(EventStatus.PENDING)
                        .retryCount(4)
                        .build();

        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                        .thenReturn(Arrays.asList(outboxEvent));
        doThrow(new RuntimeException("Error")).when(orderKafkaPublisher)
                        .publish(outboxEvent);

        outboxPublisherService.publishPendingEvents();

        verify(outboxEventService).markUpdate(outboxEvent, EventStatus.FAILED);
    }

    @Test
    void shouldNotPublishWhenNoPendingEvents() {
        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                        .thenReturn(Arrays.asList());

        outboxPublisherService.publishPendingEvents();

        verify(orderKafkaPublisher, never()).publish(any());
    }

}
