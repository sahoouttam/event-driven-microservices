package com.event.driven.stock.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.StockReservationConfirmedEvent;
import com.event.driven.common.service.events.StockReservationCreatedEvent;
import com.event.driven.common.service.events.StockReservationFailedEvent;
import com.event.driven.common.service.events.StockReservationReleasedEvent;
import com.event.driven.common.service.events.StockAddedEvent;
import com.event.driven.stock.service.dto.request.AddStockRequest;
import com.event.driven.stock.service.dto.request.ReserveStockRequest;
import com.event.driven.stock.service.dto.response.InventoryResponse;
import com.event.driven.stock.service.entity.Inventory;
import com.event.driven.stock.service.entity.Product;
import com.event.driven.stock.service.enums.EventType;
import com.event.driven.stock.service.enums.TransactionType;
import com.event.driven.stock.service.exception.ResourceNotFoundException;
import com.event.driven.stock.service.repository.InventoryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductService productService;
    private InventoryTransactionService inventoryTransactionService;
    private OutboxEventService outboxEventService;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                        ProductService productService,
                        InventoryTransactionService inventoryTransactionService,
                        OutboxEventService outboxEventService) {
        this.inventoryRepository = inventoryRepository;
        this.productService = productService;
        this.inventoryTransactionService = inventoryTransactionService;
        this.outboxEventService = outboxEventService;
    }

    public void createInventory(String sku) {
        log.info("Creating inventory for SKU={}", sku);
        Product product = productService.findBySku(sku);
        Inventory inventory = Inventory.builder()
                    .product(product)
                    .totalQuantity(0)
                    .availableQuantity(0)
                    .build();
        Inventory savedInventory = saveInventory(inventory);
        log.info("Successfully saved inventory with id={}", savedInventory.getId());
    }

    @Transactional
    public InventoryResponse addStock(AddStockRequest addStockRequest) {
        log.info("Adding stock, sku={}, quantity={}",
                        addStockRequest.getSku(), addStockRequest.getQuantity());

        Product product = productService.findBySku(addStockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setTotalQuantity(
                    inventory.getTotalQuantity() + addStockRequest.getQuantity());
        inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() + addStockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        inventoryTransactionService.saveTransaction(savedInventory,
                    addStockRequest.getQuantity(), TransactionType.STOCK_IN);

        StockAddedEvent stockAddedEvent = StockAddedEvent.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .quantity(addStockRequest.getQuantity())
                        .availableQuantity(savedInventory.getAvailableQuantity())
                        .build();

        outboxEventService.saveEvent(EventType.STOCK_ADDED, 
                        AggregateType.INVENTORY,
                        savedInventory.getId().toString(), 
                        stockAddedEvent);

        log.info("Stock added successfully, sku={}, available quantity={}",
                                addStockRequest.getSku(), savedInventory.getAvailableQuantity());

        return toResponse(product, savedInventory);
    }

    @Transactional
    public void restoreStock(AddStockRequest addStockRequest) {
        Product product = productService.findBySku(addStockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setTotalQuantity(
                    inventory.getTotalQuantity() + addStockRequest.getQuantity());
        inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() + addStockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        inventoryTransactionService.saveTransaction(savedInventory,
                    addStockRequest.getQuantity(), TransactionType.STOCK_RETURNED);

        log.info("Stock restored: sku={}, available quantity={}",
                                addStockRequest.getSku(), 
                                savedInventory.getAvailableQuantity());
    }

    @Transactional
    public InventoryResponse createReservation(ReserveStockRequest reserveStockRequest) {
        log.info("Reserving stock, sku={}, quantity={}",
                        reserveStockRequest.getSku(), reserveStockRequest.getQuantity());

        Product product = productService.findBySku(reserveStockRequest.getSku());

        Inventory inventory = findByProduct(product);
        if (inventory.getAvailableQuantity() < reserveStockRequest.getQuantity()) {
            StockReservationFailedEvent failedEvent = StockReservationFailedEvent.builder()
                            .productId(product.getId())
                            .orderId(reserveStockRequest.getOrderId())
                            .sku(product.getSku())
                            .requestedQuantity(reserveStockRequest.getQuantity())
                            .availableQuantity(inventory.getAvailableQuantity())
                            .build();
            outboxEventService.saveEvent(EventType.STOCK_RESERVATION_FAILED,
                            AggregateType.INVENTORY,
                            inventory.getId().toString(),
                            failedEvent);

            log.warn("Order failed: insufficient sku={}", reserveStockRequest.getSku());
            return toResponse(product, inventory);
        }
        inventory.setAvailableQuantity(
                        inventory.getAvailableQuantity() - reserveStockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        inventoryTransactionService.saveTransaction(savedInventory,
                        reserveStockRequest.getQuantity(), TransactionType.STOCK_RESERVED);

        StockReservationCreatedEvent reservationEvent = StockReservationCreatedEvent.builder()
                        .productId(product.getId())
                        .orderId(reserveStockRequest.getOrderId())
                        .sku(product.getSku())
                        .quantity(reserveStockRequest.getQuantity())
                        .availableQuantity(savedInventory.getAvailableQuantity())
                        .build();

        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CREATED,
                        AggregateType.INVENTORY,
                        savedInventory.getId().toString(), 
                        reservationEvent);

        log.info("Reservation updated successfully, sku={}, available={}, quantity={}",
                        reserveStockRequest.getSku(), savedInventory.getAvailableQuantity(),
                        reserveStockRequest.getQuantity());

        return toResponse(product, savedInventory);
    }

    @Transactional
    public InventoryResponse confirmReservation(ReserveStockRequest reserveStockRequest) {
        log.info("Confirming reservation for sku={}, quantity={}",
                                reserveStockRequest.getSku(), reserveStockRequest.getQuantity());

        Product product = productService.findBySku(reserveStockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setTotalQuantity(
                        inventory.getTotalQuantity() - reserveStockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        inventoryTransactionService.saveTransaction(savedInventory,
                        reserveStockRequest.getQuantity(), TransactionType.STOCK_OUT);

        StockReservationConfirmedEvent stockReservationConfirmedEvent = StockReservationConfirmedEvent.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .quantity(reserveStockRequest.getQuantity())
                        .availableQuantity(savedInventory.getAvailableQuantity())
                        .build();

        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CONFIRMED,
                        AggregateType.INVENTORY,
                        savedInventory.getId().toString(),
                        stockReservationConfirmedEvent);

        log.info("Reservation confirmed successfully, sku={}, available quantity={}",
                                reserveStockRequest.getSku(), savedInventory.getAvailableQuantity());

        return toResponse(product, savedInventory);
    }

    @Transactional
    public InventoryResponse releaseReservation(ReserveStockRequest reserveStockRequest) {
        log.info("Releasing reservation for sku={}, quantity={}",
                                reserveStockRequest.getSku(), reserveStockRequest.getQuantity());

        Product product = productService.findBySku(reserveStockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setAvailableQuantity(
                        inventory.getAvailableQuantity() + reserveStockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        inventoryTransactionService.saveTransaction(savedInventory,
                        reserveStockRequest.getQuantity(), TransactionType.STOCK_RELEASED);

        StockReservationReleasedEvent stockReservationReleasedEvent = StockReservationReleasedEvent.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .quantity(reserveStockRequest.getQuantity())
                        .availableQuantity(savedInventory.getAvailableQuantity())
                        .build();

        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_RELEASED,
                        AggregateType.INVENTORY,
                        savedInventory.getId().toString(),
                        stockReservationReleasedEvent);

        log.info("Reservation released, sku={}, available quantity={}",
                                reserveStockRequest.getSku(), savedInventory.getAvailableQuantity());

        return toResponse(product, savedInventory);
    }

    public Inventory saveInventory(Inventory inventory) {
            return inventoryRepository.save(inventory);
    }

    public Inventory findByProduct(Product product) {
            return inventoryRepository.findByProduct(product)
                                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    public InventoryResponse getInventory(String sku) {
        Product product = productService.findBySku(sku);
        Inventory inventory = findByProduct(product);
        return toResponse(product, inventory);
    }

    private InventoryResponse toResponse(Product product, Inventory inventory) {
        return InventoryResponse.builder()
                    .sku(product.getSku())
                    .productName(product.getName())
                    .totalQuantity(inventory.getTotalQuantity())
                    .availableQuantity(inventory.getAvailableQuantity())
                    .build();
        }
}
