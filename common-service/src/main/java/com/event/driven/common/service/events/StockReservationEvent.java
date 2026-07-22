package com.event.driven.common.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationEvent {
    
    private Long productId;

    private Long orderId;

    private String sku;

    private Integer quantity;

    private Integer availableQuantity;
}
