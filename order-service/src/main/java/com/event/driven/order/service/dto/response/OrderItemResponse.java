package com.event.driven.order.service.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    
    private Long id;

    private String productName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subTotal;
}
