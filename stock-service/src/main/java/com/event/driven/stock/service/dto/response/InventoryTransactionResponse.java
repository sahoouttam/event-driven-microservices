package com.event.driven.stock.service.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {
    
    private Long id;

    private String sku;

    private Integer quantity;

    private LocalDateTime createdAt;
}
