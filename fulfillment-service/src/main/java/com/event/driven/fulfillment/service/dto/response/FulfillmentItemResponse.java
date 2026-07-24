package com.event.driven.fulfillment.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentItemResponse {
    
    private Long id;

    private Long productId;

    private String productName;

    private Integer quantity;
}
