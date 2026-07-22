package com.event.driven.common.service.events;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdatedEvent {
    
    private Long productId;

    private String sku;

    private String name;

    private BigDecimal price;

    private Boolean active;
}
