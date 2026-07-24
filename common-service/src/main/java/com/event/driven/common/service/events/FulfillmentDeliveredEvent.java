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
public class FulfillmentDeliveredEvent {
    
    private Long fulfillmentId;

    private Long orderId;

    private LocalDateTime deliveredAt;
}
