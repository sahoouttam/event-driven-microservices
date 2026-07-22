package com.event.driven.payment.service.entity;

import java.time.LocalDateTime;

import com.event.driven.common.service.entity.BaseEntity;
import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.enums.EventStatus;
import com.event.driven.payment.service.enums.EventType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "outbox_events")
public class OutboxEvent extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String payload;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.PENDING;

    @Builder.Default
    private Integer retryCount = 0;

    private LocalDateTime publishedAt;

    private String errorMessage;
}
