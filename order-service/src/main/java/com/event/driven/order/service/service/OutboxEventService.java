package com.event.driven.order.service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.common.service.exceptions.EventSerializationException;
import com.event.driven.order.service.entity.OutboxEvent;
import com.event.driven.order.service.enums.EventType;
import com.event.driven.order.service.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutboxEventService {
    

    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OutboxEventService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public List<OutboxEvent> findByEventStatus(EventStatus eventStatus, Integer retryCount) {
        return outboxEventRepository
                .findTop100ByEventStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                    eventStatus,
                    retryCount
                );
    }

    public <T> void saveEvent(EventType eventType, AggregateType aggregateType, String aggregateId, T payload) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .aggregateType(aggregateType)
                        .aggregateId(aggregateId)
                        .eventType(eventType)
                        .payload(objectMapper.writeValueAsString(payload))
                        .eventStatus(EventStatus.PENDING)
                        .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new EventSerializationException(
                "Unable to serialize event", ex);
        }
    }

    @Transactional
    public void markUpdate(OutboxEvent outboxEvent, EventStatus eventStatus) {
        outboxEvent.setEventStatus(eventStatus);
        outboxEventRepository.save(outboxEvent);
    }

    public void incrementRetryCount(OutboxEvent outboxEvent) {
        outboxEvent.setRetryCount(outboxEvent.getRetryCount() + 1);
        outboxEventRepository.save(outboxEvent);
    }
    
}
