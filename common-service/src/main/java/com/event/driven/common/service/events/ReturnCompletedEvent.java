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
public class ReturnCompletedEvent {
    
    private Long returnId;

    private Long orderId;

    private Long fulfillmentId;

    private LocalDateTime completedAt;
}
