package com.event.driven.order.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.order.service.entity.OutboxEvent;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByEventStatusAndRetryCountLessThanOrderByCreatedAtAsc(
        EventStatus eventStatus,
        Integer retryCount);
      
    Optional<OutboxEvent> findByEventId(String eventId);
}
