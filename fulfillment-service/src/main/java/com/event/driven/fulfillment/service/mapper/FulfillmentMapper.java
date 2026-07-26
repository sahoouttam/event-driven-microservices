package com.event.driven.fulfillment.service.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.event.driven.common.service.events.ReturnItemEvent;
import com.event.driven.fulfillment.service.dto.response.FulfillmentItemResponse;
import com.event.driven.fulfillment.service.dto.response.FulfillmentResponse;
import com.event.driven.fulfillment.service.dto.response.ReturnItemResponse;
import com.event.driven.fulfillment.service.dto.response.ReturnResponse;
import com.event.driven.fulfillment.service.entity.Fulfillment;
import com.event.driven.fulfillment.service.entity.FulfillmentItem;
import com.event.driven.fulfillment.service.entity.Return;
import com.event.driven.fulfillment.service.entity.ReturnItem;

@Component
public class FulfillmentMapper {

    public FulfillmentResponse toResponse(Fulfillment fulfillment) {
        return FulfillmentResponse.builder()
                        .id(fulfillment.getId())
                        .orderId(fulfillment.getOrderId())
                        .customerId(fulfillment.getCustomerId())
                        .fulfillmentStatus(fulfillment.getFulfillmentStatus())
                        .trackingNumber(fulfillment.getTrackingNumber())
                        .carrier(fulfillment.getCarrier())
                        .shippedAt(fulfillment.getShippedAt())
                        .deliveredAt(fulfillment.getDeliveredAt())
                        .fulfillmentItemResponses(
                            fulfillment.getFulfillmentItems()
                                    .stream()
                                    .map(this::toResponse)
                                    .collect(Collectors.toList())
                        )
                        .build();
    }
    
    public FulfillmentItemResponse toResponse(FulfillmentItem fulfillmentItem) {
        return FulfillmentItemResponse.builder()
                        .id(fulfillmentItem.getId())
                        .productId(fulfillmentItem.getProductId())
                        .productName(fulfillmentItem.getProductName())
                        .quantity(fulfillmentItem.getQuantity())
                        .build();
    }

    public ReturnResponse toResponse(Return returnEntity) {
        return ReturnResponse.builder()
                        .id(returnEntity.getId())
                        .orderId(returnEntity.getOrderId())
                        .fulfillmentId(returnEntity.getFulfillmentId())
                        .returnStatus(returnEntity.getReturnStatus())
                        .returnItemResponses(
                            returnEntity.getReturnItems()
                                .stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList())
                        )
                        .build();
    }

    public ReturnItemResponse toResponse(ReturnItem returnItem) {
        return ReturnItemResponse.builder()
                        .id(returnItem.getId())
                        .productId(returnItem.getProductId())
                        .productName(returnItem.getProductName())
                        .quantity(returnItem.getQuantity())
                        .build();
    }

    public ReturnItemEvent toEvent(ReturnItem returnItem) {
        return ReturnItemEvent.builder()
                        .returnItemId(returnItem.getId())
                        .productId(returnItem.getProductId())
                        .productName(returnItem.getProductName())
                        .quantity(returnItem.getQuantity())
                        .build();
    }
}
