package com.event.driven.common.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemEvent {

    private Long returnItemId;
    
    private Long productId;

    private String sku;

    private String productName;

    private Integer quantity;
}
