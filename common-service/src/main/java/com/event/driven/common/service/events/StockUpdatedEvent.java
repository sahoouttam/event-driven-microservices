package com.event.driven.common.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdatedEvent {

    private Long productId;

    private String sku;

    private Integer quantity;

    private Integer availableQuantity;
}
