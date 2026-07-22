package com.event.driven.order.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.order.service.entity.OutboxEvent;
import com.event.driven.order.service.kafka.OrderKafkaPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutboxPublisherService {
    
    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventService outboxEventService;
    private final OrderKafkaPublisher orderKafkaPublisher;

    @Autowired
    public OutboxPublisherService(OutboxEventService outboxEventService, 
                                    OrderKafkaPublisher orderKafkaPublisher) {
        this.outboxEventService = outboxEventService;
        this.orderKafkaPublisher = orderKafkaPublisher;
    }

    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> outboxEvents = outboxEventService.findByEventStatus(
                                    EventStatus.PENDING, MAX_RETRY_COUNT);
        log.info("Found {} pending outbox event(s).", outboxEvents.size());

        for (OutboxEvent outboxEvent : outboxEvents) {
            publishEvent(outboxEvent);
        }
    }

    private void publishEvent(OutboxEvent outboxEvent) {
        try {
            orderKafkaPublisher.publish(outboxEvent);
            outboxEventService.markUpdate(outboxEvent, EventStatus.PUBLISHED);
            log.info("Successfully published eventId={}, eventType={}",
                    outboxEvent.getEventId(), outboxEvent.getEventType());
        } catch (Exception ex) {
            log.error("Failed to publish eventId={}", outboxEvent.getId(), ex);
            handleFailure(outboxEvent);
        }
    }

    private void handleFailure(OutboxEvent outboxEvent) {
        int retryCount = outboxEvent.getRetryCount() + 1;
        if (retryCount >= MAX_RETRY_COUNT) {
            outboxEventService.markUpdate(outboxEvent, EventStatus.FAILED);
            log.error("Event {} moved to failed state.", outboxEvent.getId());
            return;
        }
        outboxEventService.incrementRetryCount(outboxEvent);
    }
}