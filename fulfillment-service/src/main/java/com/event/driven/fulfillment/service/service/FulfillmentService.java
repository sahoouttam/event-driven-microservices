package com.event.driven.fulfillment.service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.FulfillmentDeliveredEvent;
import com.event.driven.common.service.events.FulfillmentShippedEvent;
import com.event.driven.common.service.events.OrderConfirmedEvent;
import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.fulfillment.service.client.AccountClient;
import com.event.driven.fulfillment.service.dto.response.AddressResponse;
import com.event.driven.fulfillment.service.dto.response.CustomerResponse;
import com.event.driven.fulfillment.service.dto.response.FulfillmentResponse;
import com.event.driven.fulfillment.service.entity.Fulfillment;
import com.event.driven.fulfillment.service.entity.FulfillmentItem;
import com.event.driven.fulfillment.service.enums.EventType;
import com.event.driven.fulfillment.service.enums.FulfillmentStatus;
import com.event.driven.fulfillment.service.exception.FulfillmentException;
import com.event.driven.fulfillment.service.mapper.FulfillmentMapper;
import com.event.driven.fulfillment.service.repository.FulfillmentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FulfillmentService {

    private final FulfillmentRepository fulfillmentRepository;
    private final FulfillmentItemService fulfillmentItemService;
    private final OutboxEventService outboxEventService;
    private final FulfillmentMapper fulfillmentMapper;
    private final AccountClient accountClient;
    
    @Autowired
    public FulfillmentService(FulfillmentRepository fulfillmentRepository,
                                FulfillmentItemService fulfillmentItemService,
                                OutboxEventService outboxEventService,
                                FulfillmentMapper fulfillmentMapper,
                                AccountClient accountClient) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.fulfillmentItemService = fulfillmentItemService;
        this.outboxEventService = outboxEventService;
        this.fulfillmentMapper = fulfillmentMapper;
        this.accountClient = accountClient;
    }

    @Transactional
    public void createFulfillment(OrderConfirmedEvent orderConfirmedEvent) {
        CompletableFuture<CustomerResponse> customerFuture = 
                accountClient.getCustomer(orderConfirmedEvent.getCustomerId());
        CompletableFuture<AddressResponse> addressFuture = 
                accountClient.getAddress(orderConfirmedEvent.getCustomerId(), 
                                        orderConfirmedEvent.getShippingAddressId());
        CompletableFuture.allOf(customerFuture, addressFuture).join();
        CustomerResponse customerResponse = customerFuture.join();
        AddressResponse addressResponse = addressFuture.join();
        Fulfillment fulfillment = Fulfillment.builder()
                    .orderId(orderConfirmedEvent.getOrderId())
                    .customerId(orderConfirmedEvent.getCustomerId())
                    .customerName(customerResponse.getName())
                    .customerPhone(customerResponse.getPhone())
                    .addressId(addressResponse.getAddressId())
                    .shippingStreet(addressResponse.getStreet())
                    .shippingCity(addressResponse.getCity())
                    .shippingState(addressResponse.getState())
                    .fulfillmentStatus(FulfillmentStatus.PENDING)
                    .build();
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);
        List<FulfillmentItem> fulfillmentItems = new ArrayList<>();
        for (OrderItemEvent orderItemEvent : orderConfirmedEvent.getOrderItemEvents()) {
            FulfillmentItem fulfillmentItem = fulfillmentItemService
                                .createFulfillmentItem(savedFulfillment, orderItemEvent);
            fulfillmentItems.add(fulfillmentItem);
        }
        savedFulfillment.setFulfillmentItems(fulfillmentItems);

        Fulfillment updatedFulfillment = fulfillmentRepository.save(savedFulfillment);
        log.info("Created fulfillment {} for order {} with {} items", 
                updatedFulfillment.getId(),
                updatedFulfillment.getOrderId(),
                updatedFulfillment.getFulfillmentItems().size());  
    }

    @Transactional
    public FulfillmentResponse startPicking(Long id) {
        Fulfillment fulfillment = findFulfillment(id);
        if (fulfillment.getFulfillmentStatus() != FulfillmentStatus.PENDING) {
            throw new FulfillmentException("Fulfillment not in PENDING state");
        }
        fulfillment.setFulfillmentStatus(FulfillmentStatus.PICKING);
        Fulfillment savedFulfillment = saveFulfillment(fulfillment);
        return fulfillmentMapper.toResponse(savedFulfillment);
    }

    @Transactional
    public FulfillmentResponse markPacked(Long id) {
        Fulfillment fulfillment = findFulfillment(id);
        if (fulfillment.getFulfillmentStatus() != FulfillmentStatus.PICKING) {
            throw new FulfillmentException("Fulfillment not in PICKED state");
        }
        fulfillment.setFulfillmentStatus(FulfillmentStatus.PACKED);
        Fulfillment savedFulfillment = saveFulfillment(fulfillment);
        return fulfillmentMapper.toResponse(savedFulfillment);
    }

    @Transactional
    public FulfillmentResponse shipOrder(Long id) {
        Fulfillment fulfillment = findFulfillment(id);
        if (fulfillment.getFulfillmentStatus() != FulfillmentStatus.PACKED) {
            throw new FulfillmentException("Fulfillment not in PACKED state");
        }
        String trackingNumber = generateTrackingNumber();
        String carrier = createCarrier();
        fulfillment.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
        fulfillment.setTrackingNumber(trackingNumber);
        fulfillment.setCarrier(carrier);
        Fulfillment savedFulfillment = saveFulfillment(fulfillment);
        FulfillmentShippedEvent fulfillmentShippedEvent = FulfillmentShippedEvent.builder()
                                .fulfillmentId(savedFulfillment.getId())
                                .orderId(savedFulfillment.getOrderId())
                                .trackingNumber(trackingNumber)
                                .carrier(carrier)
                                .shippedAt(savedFulfillment.getShippedAt())
                                .build();
        outboxEventService.saveEvent(EventType.FULFILLMENT_SHIPPED,
                             AggregateType.FULFILLMENT, 
                             id.toString(), 
                             fulfillmentShippedEvent);
        log.info("Order shipped: {}, tracking: {}", id, trackingNumber);
        return fulfillmentMapper.toResponse(savedFulfillment);
    }

    @Transactional
    public FulfillmentResponse markDelivered(Long id) {
        Fulfillment fulfillment = findFulfillment(id);
        if (fulfillment.getFulfillmentStatus() != FulfillmentStatus.SHIPPED) {
            throw new FulfillmentException("Fulfillment not in SHIPPED state");
        }
        fulfillment.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
        fulfillment.setDeliveredAt(LocalDateTime.now());
        Fulfillment savedFulfillment = saveFulfillment(fulfillment);
        FulfillmentDeliveredEvent fulfillmentDeliveredEvent = FulfillmentDeliveredEvent.builder()
                                .fulfillmentId(savedFulfillment.getId())
                                .orderId(savedFulfillment.getOrderId())
                                .deliveredAt(savedFulfillment.getDeliveredAt())
                                .build();
        outboxEventService.saveEvent(EventType.FULFILLMENT_DELIVERED,
                             AggregateType.FULFILLMENT, 
                             id.toString(), 
                             fulfillmentDeliveredEvent);
        log.info("Order delivered: {}, delivered at: {}", id, savedFulfillment.getDeliveredAt());
        return fulfillmentMapper.toResponse(savedFulfillment);
    }

    @Transactional
    public void cancelFulfillment(Long id) {
        Fulfillment fulfillment = findFulfillment(id);
        if (fulfillment.getFulfillmentStatus() == FulfillmentStatus.DELIVERED) {
            throw new FulfillmentException("Cannot cancel fulfillment: " + id);
        }
        fulfillment.setFulfillmentStatus(FulfillmentStatus.CANCELLED);
        Fulfillment savedFulfillment = saveFulfillment(fulfillment);
        log.info("Fulfillment cancelled: {}, order: {}", id, savedFulfillment.getOrderId());
    }

    @Transactional
    public FulfillmentResponse getFulfillmentByOrder(Long orderId) {
        return fulfillmentRepository.findByOrderId(orderId)
                    .map(fulfillmentMapper::toResponse)
                    .orElseThrow(() -> new FulfillmentException("Fulfillment not found"));
    }

    @Transactional
    public FulfillmentResponse getFulfillment(Long fulfillmentId) {
        Fulfillment fulfillment = findFulfillment(fulfillmentId);
        return fulfillmentMapper.toResponse(fulfillment);
    }

    private Fulfillment findFulfillment(Long id) {
        return fulfillmentRepository.findById(id)
                    .orElseThrow(() -> new FulfillmentException("Fulfillment not found"));
    }

    public Fulfillment saveFulfillment(Fulfillment fulfillment) {
        return fulfillmentRepository.save(fulfillment);
    }

    private String createCarrier() {
        return "CARRIER-" + UUID.randomUUID().toString().substring(0, 5); 
    }
    
    public String generateTrackingNumber() {
        return "TRACKING-" + UUID.randomUUID().toString().substring(0, 4);
    }
}
