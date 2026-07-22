package com.event.driven.stock.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    
    private String sku;

    private String productName;

    private Long inventoryTransactionId;

    private Integer totalQuantity;

    private Integer availableQuantity;
}
