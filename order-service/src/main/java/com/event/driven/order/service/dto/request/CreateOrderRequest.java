package com.event.driven.order.service.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    private Long customerId;

    private List<OrderItemRequest> orderItemRequests;
}
