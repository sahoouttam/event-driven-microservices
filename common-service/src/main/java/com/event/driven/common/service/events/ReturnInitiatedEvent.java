package com.event.driven.common.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnInitiatedEvent {
    
    private Long returnId;

    private Long orderId;

    private Long fulfillmentId;
}
