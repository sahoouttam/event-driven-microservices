package com.event.driven.stock.service.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;

    private String sku;

    private String name;

    private BigDecimal price;

    private boolean active;
}
