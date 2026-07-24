package com.event.driven.fulfillment.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemRequest {
    
    private Long productId;

    private String productName;

    private Integer quantity;
}
