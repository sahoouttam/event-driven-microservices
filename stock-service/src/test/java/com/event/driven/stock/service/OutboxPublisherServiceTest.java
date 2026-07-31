package com.event.driven.stock.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.stock.service.entity.OutboxEvent;
import com.event.driven.stock.service.kafka.StockKafkaPubisher;
import com.event.driven.stock.service.service.OutboxEventService;
import com.event.driven.stock.service.service.OutboxPublisherService;

@ExtendWith(MockitoExtension.class)
public class OutboxPublisherServiceTest {
    
    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private StockKafkaPubisher stockKafkaPublisher;

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

        verify(stockKafkaPublisher, times(2)).publish(any(OutboxEvent.class));
        verify(outboxEventService).markUpdate(outboxEvent1, EventStatus.PUBLISHED);
        verify(outboxEventService).markUpdate(outboxEvent2, EventStatus.PUBLISHED);
    }

    @Test
    void shouldHandlePublishingFailure() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-5")
                        .eventStatus(EventStatus.PENDING)
                        .retryCount(0)
                        .build();

        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                        .thenReturn(Arrays.asList(outboxEvent));
        doThrow(new RuntimeException("kafka error"))
                        .when(stockKafkaPublisher).publish(outboxEvent);
        outboxPublisherService.publishPendingEvents();

        verify(outboxEventService).incrementRetryCount(outboxEvent);
        verify(outboxEventService, never()).markUpdate(outboxEvent, EventStatus.PUBLISHED);
    }

    @Test
    void shouldMarkEventAsFailedAfterMaxRetries() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId("event-3")
                        .eventStatus(EventStatus.PENDING)
                        .retryCount(4)
                        .build();

        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                        .thenReturn(Arrays.asList(outboxEvent));
        doThrow(new RuntimeException("kafka error"))
                        .when(stockKafkaPublisher).publish(outboxEvent);
        outboxPublisherService.publishPendingEvents();

        verify(outboxEventService).markUpdate(outboxEvent, EventStatus.FAILED);
        verify(outboxEventService, never()).incrementRetryCount(outboxEvent);;
    }

    @Test
    void shouldNotPublishWhenNoPendingEvents() {
        when(outboxEventService.findByEventStatus(EventStatus.PENDING, 5))
                    .thenReturn(new ArrayList<>());
        outboxPublisherService.publishPendingEvents();

        verify(stockKafkaPublisher, never()).publish(any());
    }
}
