package com.event.driven.fulfillment.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.fulfillment.service.dto.response.FulfillmentItemResponse;
import com.event.driven.fulfillment.service.entity.Fulfillment;
import com.event.driven.fulfillment.service.entity.FulfillmentItem;
import com.event.driven.fulfillment.service.repository.FulfillmentItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FulfillmentItemService {
    
    private final FulfillmentItemRepository fulfillmentItemRepository;

    @Autowired
    public FulfillmentItemService(FulfillmentItemRepository fulfillmentItemRepository) {
        this.fulfillmentItemRepository = fulfillmentItemRepository;
    }

    public void createFulfillmentItem(Fulfillment fulfillment, 
                                                            OrderItemEvent orderItemEvent) {
        FulfillmentItem fulfillmentItem = FulfillmentItem.builder()
                        .fulfillment(fulfillment)
                        .productId(orderItemEvent.getProductId())
                        .productName(orderItemEvent.getProductName())
                        .quantity(orderItemEvent.getQuantity())
                        .build();
        FulfillmentItem savedFulfillmentItem = fulfillmentItemRepository
                                                    .save(fulfillmentItem);
        log.info("Created fulfillment item {}, name {}, and quantity {}",
                savedFulfillmentItem.getId(),
                savedFulfillmentItem.getProductName(),
                savedFulfillmentItem.getQuantity());
    }

    private FulfillmentItemResponse mapToResponse(FulfillmentItem fulfillmentItem) {
        return FulfillmentItemResponse.builder()
                    .id(fulfillmentItem.getId())
                    .productId(fulfillmentItem.getProductId())
                    .productName(fulfillmentItem.getProductName())
                    .quantity(fulfillmentItem.getQuantity())
                    .build();
    }
}
