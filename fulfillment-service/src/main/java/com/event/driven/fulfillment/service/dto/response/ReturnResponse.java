package com.event.driven.fulfillment.service.dto.response;

import java.util.List;

import com.event.driven.fulfillment.service.enums.ReturnStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    
    private Long id;

    private Long orderId;

    private Long fulfillmentId;

    private ReturnStatus returnStatus;

    private List<ReturnItemResponse> returnItemResponses;
}
