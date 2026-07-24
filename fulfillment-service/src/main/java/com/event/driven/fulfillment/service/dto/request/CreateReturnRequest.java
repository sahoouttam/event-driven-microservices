package com.event.driven.fulfillment.service.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {
    
    private Long orderId;

    private List<ReturnItemRequest> returnItemRequests;
}
