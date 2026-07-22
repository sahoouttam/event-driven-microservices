package com.event.driven.common.service.events;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope {
    
    private String eventId;

    private String eventType;

    private String aggregateId;

    private String aggregateType;

    private LocalDateTime timeStamp;

    private String payload;
}
