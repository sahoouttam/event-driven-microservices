package com.event.driven.fulfillment.service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.ReturnCompletedEvent;
import com.event.driven.common.service.events.ReturnInitiatedEvent;
import com.event.driven.common.service.events.ReturnItemEvent;
import com.event.driven.fulfillment.service.dto.request.CreateReturnRequest;
import com.event.driven.fulfillment.service.dto.request.ReturnItemRequest;
import com.event.driven.fulfillment.service.dto.response.FulfillmentResponse;
import com.event.driven.fulfillment.service.dto.response.ReturnResponse;
import com.event.driven.fulfillment.service.entity.Return;
import com.event.driven.fulfillment.service.entity.ReturnItem;
import com.event.driven.fulfillment.service.enums.EventType;
import com.event.driven.fulfillment.service.enums.FulfillmentStatus;
import com.event.driven.fulfillment.service.enums.ReturnStatus;
import com.event.driven.fulfillment.service.exception.FulfillmentException;
import com.event.driven.fulfillment.service.exception.ReturnException;
import com.event.driven.fulfillment.service.mapper.FulfillmentMapper;
import com.event.driven.fulfillment.service.repository.ReturnRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReturnService {
    
    private final ReturnRepository returnRepository;
    private final ReturnItemService returnItemService;
    private final FulfillmentService fulfillmentService;
    private final OutboxEventService outboxEventService;
    private final FulfillmentMapper fulfillmentMapper;

    @Autowired
    public ReturnService(ReturnRepository returnRepository, 
                        ReturnItemService returnItemService,
                        FulfillmentService fulfillmentService,
                        OutboxEventService outboxEventService, 
                        FulfillmentMapper fulfillmentMapper) {
        this.returnRepository = returnRepository;
        this.returnItemService = returnItemService;
        this.fulfillmentService = fulfillmentService;
        this.outboxEventService = outboxEventService;
        this.fulfillmentMapper = fulfillmentMapper;
    }

    public ReturnResponse initiateReturn(CreateReturnRequest createReturnRequest) {
        FulfillmentResponse fulfillment = fulfillmentService
                        .getFulfillmentByOrder(createReturnRequest.getOrderId());
        if (fulfillment.getFulfillmentStatus() != FulfillmentStatus.DELIVERED) {
            throw new FulfillmentException("Can only return delivered orders");
        }

        Return returnEntity = Return.builder()
                    .orderId(createReturnRequest.getOrderId())
                    .customerId(fulfillment.getCustomerId())
                    .fulfillmentId(fulfillment.getId())
                    .returnStatus(ReturnStatus.INITIATED)
                    .build();
        Return savedReturn = saveReturn(returnEntity);

        List<ReturnItem> returnItems = new ArrayList<>();
        for (ReturnItemRequest returnItemRequest : createReturnRequest.getReturnItemRequests()) {
            ReturnItem returnItem = returnItemService.createReturnItem(savedReturn, returnItemRequest);
            returnItems.add(returnItem);
        }
        savedReturn.setReturnItems(returnItems);
        Return updatedReturn = returnRepository.save(savedReturn);
        ReturnInitiatedEvent returnInitiatedEvent = ReturnInitiatedEvent.builder()
                    .returnId(updatedReturn.getId())
                    .orderId(updatedReturn.getOrderId())
                    .fulfillmentId(updatedReturn.getFulfillmentId())
                    .build();
        outboxEventService.saveEvent(EventType.RETURN_INITIATED, 
                                    AggregateType.RETURN, 
                                    updatedReturn.getId().toString(), 
                                    returnInitiatedEvent);

        log.info("Return initiated for order {}", createReturnRequest.getOrderId());
        return fulfillmentMapper.toResponse(updatedReturn);
    }

    public ReturnResponse approveReturn(Long id) {
        Return returnEntity = findReturn(id);
        if (returnEntity.getReturnStatus() != ReturnStatus.INITIATED) {
            throw new ReturnException("Return is not in INITIATED state");
        }
        returnEntity.setReturnStatus(ReturnStatus.APPROVED);
        Return savedReturn = saveReturn(returnEntity);

        log.info("Return {} approved for fulfillment {}", 
                                id, savedReturn.getFulfillmentId());
        return fulfillmentMapper.toResponse(savedReturn);
    }

    public ReturnResponse rejectReturn(Long id) {
        Return returnEntity = findReturn(id);
        if (returnEntity.getReturnStatus() != ReturnStatus.INITIATED) {
            throw new ReturnException("Return is not in INITIATED state");
        }
        returnEntity.setReturnStatus(ReturnStatus.REJECTED);
        Return savedReturn = saveReturn(returnEntity);

        log.info("Return {} rejected for fulfillment {}", 
                                id, savedReturn.getFulfillmentId());
        return fulfillmentMapper.toResponse(savedReturn);
    }

    public ReturnResponse markReceived(Long id) {
        Return returnEntity = findReturn(id);
        if (returnEntity.getReturnStatus() != ReturnStatus.APPROVED) {
            throw new ReturnException("Return is not in APPROVED state");
        }
        returnEntity.setReturnStatus(ReturnStatus.RECEIVED);
        returnEntity.setReceivedAt(LocalDateTime.now());
        Return savedReturn = saveReturn(returnEntity);

        log.info("Return {} received for fulfillment {}", 
                                id, savedReturn.getFulfillmentId());
        return fulfillmentMapper.toResponse(savedReturn);
    }

    public ReturnResponse markCompleted(Long id) {
        Return returnEntity = findReturn(id);
        if (returnEntity.getReturnStatus() != ReturnStatus.RECEIVED) {
            throw new ReturnException("Return is not in COMPLETED state");
        }
        returnEntity.setReturnStatus(ReturnStatus.COMPLETED);
        returnEntity.setCompletedAt(LocalDateTime.now());
        Return savedReturn = saveReturn(returnEntity);

        List<ReturnItemEvent> returnItemEvents = returnItemService.findReturnItems(returnEntity)
                                    .stream()
                                    .map(fulfillmentMapper::toEvent)
                                    .collect(Collectors.toList());

        ReturnCompletedEvent returnCompletedEvent = ReturnCompletedEvent.builder()
                        .returnId(id)
                        .orderId(savedReturn.getOrderId())
                        .fulfillmentId(savedReturn.getFulfillmentId())
                        .completedAt(savedReturn.getCompletedAt())
                        .returnItemEvents(returnItemEvents)
                        .build();

        outboxEventService.saveEvent(EventType.RETURN_COMPLETED,
                                    AggregateType.RETURN, 
                                    id.toString(), 
                                    returnCompletedEvent);

        log.info("Return {} completed", id);
        return fulfillmentMapper.toResponse(savedReturn);
    }

    public ReturnResponse getReturn(Long returnId) {
        return fulfillmentMapper.toResponse(findReturn(returnId));
    }

    public Return findReturn(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ReturnException("Return not found: " + id));
    }

    public Return saveReturn(Return returnEntity) {
        return returnRepository.save(returnEntity);
    }
}
