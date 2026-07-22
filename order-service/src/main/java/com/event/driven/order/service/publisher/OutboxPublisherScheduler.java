package com.event.driven.order.service.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.event.driven.order.service.service.OutboxPublisherService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutboxPublisherScheduler {
    
    private final OutboxPublisherService outboxPublisherService;

    @Autowired
    public OutboxPublisherScheduler(OutboxPublisherService outboxPublisherService) {
        this.outboxPublisherService = outboxPublisherService;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        log.info("Starting outbox publisher scheduler");
        outboxPublisherService.publishPendingEvents();
        log.info("Completed outbox publisher scheduler");
    }
}
