package com.event.driven.stock.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.StockReservationEvent;
import com.event.driven.common.service.events.StockReservationFailedEvent;
import com.event.driven.common.service.events.StockUpdatedEvent;
import com.event.driven.stock.service.dto.request.StockOperationRequest;
import com.event.driven.stock.service.dto.response.InventoryResponse;
import com.event.driven.stock.service.entity.Inventory;
import com.event.driven.stock.service.entity.InventoryTransaction;
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
    public InventoryResponse addStock(StockOperationRequest stockRequest) {
        log.info("Adding stock, sku={}, quantity={}",
                stockRequest.getSku(), stockRequest.getQuantity());

        Product product = productService.findBySku(stockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setTotalQuantity(
                inventory.getTotalQuantity() + stockRequest.getQuantity());
        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + stockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        InventoryTransaction savedTransaction = inventoryTransactionService
                        .saveTransaction(savedInventory, 
                                        stockRequest.getQuantity(), 
                                        TransactionType.STOCK_IN);


        StockUpdatedEvent stockUpdatedEvent = StockUpdatedEvent.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .quantity(stockRequest.getQuantity())
                .availableQuantity(savedInventory.getAvailableQuantity())
                .build();

        outboxEventService.saveEvent(EventType.STOCK_UPDATED, AggregateType.INVENTORY,
                savedInventory.getId().toString(), stockUpdatedEvent);

        log.info("Stock added successfully, sku={}, available quantity={}",
                stockRequest.getSku(), savedInventory.getAvailableQuantity());

        return toResponse(product, savedInventory, savedTransaction);
    }

    @Transactional
    public InventoryResponse updateReservation(StockOperationRequest stockRequest) {
        log.info("Updating reservation, sku={}, quantity={}", 
                    stockRequest.getSku(), stockRequest.getQuantity());     

        Product product = productService.findBySku(stockRequest.getSku());

        Inventory inventory = findByProduct(product);
        if (inventory.getAvailableQuantity() < stockRequest.getQuantity()) {
            StockReservationFailedEvent failedEvent = StockReservationFailedEvent.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .requestedQuantity(stockRequest.getQuantity())
                        .availableQuantity(inventory.getAvailableQuantity())
                        .build();
            outboxEventService.saveEvent(EventType.STOCK_RESERVATION_FAILED, 
                                        AggregateType.INVENTORY,
                                        inventory.getId().toString(),
                                        failedEvent);   
                                 
            log.warn("Order failed: insufficient sku={}", stockRequest.getSku());
            return null;
        }
        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - stockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        InventoryTransaction savedTransaction = inventoryTransactionService
                .saveTransaction(savedInventory, 
                        stockRequest.getQuantity(), 
                        TransactionType.STOCK_RESERVED);
        
        StockReservationEvent reservationEvent = StockReservationEvent.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .quantity(stockRequest.getQuantity())
                        .availableQuantity(savedInventory.getAvailableQuantity())
                        .build();
        
        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_UPDATED, 
                            AggregateType.INVENTORY, 
                            savedInventory.getId().toString(), reservationEvent);

        log.info("Reservation updated successfully, sku={}, available={}, quantity={}", 
                stockRequest.getSku(), savedInventory.getAvailableQuantity(), 
                                stockRequest.getQuantity());

        return toResponse(product, savedInventory, savedTransaction);
    }

    @Transactional
    public InventoryResponse confirmReservation(StockOperationRequest stockRequest) {
        log.info("Confirming reservation for sku={}, quantity={}",
                stockRequest.getSku(), stockRequest.getQuantity());

        Product product = productService.findBySku(stockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setTotalQuantity(
                inventory.getTotalQuantity() - stockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        InventoryTransaction savedTransaction = inventoryTransactionService
                .saveTransaction(savedInventory, 
                        stockRequest.getQuantity(), 
                        TransactionType.STOCK_OUT);

        StockUpdatedEvent addedEvent = StockUpdatedEvent.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .quantity(stockRequest.getQuantity())
                .availableQuantity(savedInventory.getAvailableQuantity())
                .build();

        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_CONFIRMED, 
                                AggregateType.INVENTORY,
                                savedInventory.getId().toString(), 
                                addedEvent);

        log.info("Reservation confirmed successfully, sku={}, available quantity={}",
                stockRequest.getSku(), savedInventory.getAvailableQuantity());

        return toResponse(product, savedInventory, savedTransaction);
    }

    @Transactional
    public InventoryResponse releaseReservation(StockOperationRequest stockRequest) {
        log.info("releasing reservation for sku={}, quantity={}",
                stockRequest.getSku(), stockRequest.getQuantity());

        Product product = productService.findBySku(stockRequest.getSku());
        Inventory inventory = findByProduct(product);

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + stockRequest.getQuantity());
        Inventory savedInventory = saveInventory(inventory);

        InventoryTransaction savedTransaction = inventoryTransactionService
                .saveTransaction(savedInventory, 
                        stockRequest.getQuantity(), 
                        TransactionType.STOCK_RELEASED);

        StockUpdatedEvent addedEvent = StockUpdatedEvent.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .quantity(stockRequest.getQuantity())
                .availableQuantity(savedInventory.getAvailableQuantity())
                .build();

        outboxEventService.saveEvent(EventType.STOCK_RESERVATION_RELEASED, 
                                AggregateType.INVENTORY,
                                savedInventory.getId().toString(), 
                                addedEvent);

        log.info("Reservation released, sku={}, available quantity={}",
                stockRequest.getSku(), savedInventory.getAvailableQuantity());

        return toResponse(product, savedInventory, savedTransaction);
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
        return toResponse(product, inventory, null);
    }

    private InventoryResponse toResponse(Product product, Inventory inventory, InventoryTransaction transaction) {
        return InventoryResponse.builder()
                .sku(product.getSku())
                .productName(product.getName())
                .inventoryTransactionId(transaction.getId())
                .totalQuantity(inventory.getTotalQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .build();
    }            
}
