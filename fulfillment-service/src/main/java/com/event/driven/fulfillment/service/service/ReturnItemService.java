package com.event.driven.fulfillment.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.events.ReturnItemEvent;
import com.event.driven.fulfillment.service.dto.request.ReturnItemRequest;
import com.event.driven.fulfillment.service.entity.Return;
import com.event.driven.fulfillment.service.entity.ReturnItem;
import com.event.driven.fulfillment.service.repository.ReturnItemRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReturnItemService {

    private final ReturnItemRepository returnItemRepository;

    @Autowired
    public ReturnItemService(ReturnItemRepository returnItemRepository) {
        this.returnItemRepository = returnItemRepository;
    }

    public void createReturnItem(Return returnEntity, ReturnItemRequest returnItemRequest) {
        ReturnItem returnItem = ReturnItem.builder()
                    .returnEntity(returnEntity)
                    .productId(returnItemRequest.getProductId())
                    .productName(returnItemRequest.getProductName())
                    .quantity(returnItemRequest.getQuantity())
                    .build();
        ReturnItem savedReturnItem = returnItemRepository.save(returnItem);
        log.info("Created return item {} for product: {}, name: {}", 
                savedReturnItem.getId(),
                returnItemRequest.getProductId(),
                returnItemRequest.getProductName());
    }

    public List<ReturnItem> findReturnItems(Return returnEntity) {
        return returnItemRepository.findByReturn(returnEntity);
    } 
}
