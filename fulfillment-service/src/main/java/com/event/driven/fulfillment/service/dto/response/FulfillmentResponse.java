package com.event.driven.fulfillment.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.event.driven.fulfillment.service.enums.FulfillmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentResponse {
    
    private Long id;

    private Long orderId;

    private Long customerId;

    private FulfillmentStatus fulfillmentStatus;

    private String trackingNumber;

    private String carrier;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    private List<FulfillmentItemResponse> fulfillmentItemResponses;
}
