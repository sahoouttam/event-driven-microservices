package com.event.driven.stock.service.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    private String sku;

    private String name;

    private BigDecimal price;
}
