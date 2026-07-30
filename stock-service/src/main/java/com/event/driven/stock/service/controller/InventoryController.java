package com.event.driven.stock.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.stock.service.dto.request.AddStockRequest;
import com.event.driven.stock.service.dto.request.ReserveStockRequest;
import com.event.driven.stock.service.dto.response.InventoryResponse;
import com.event.driven.stock.service.service.InventoryService;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String sku) {
        InventoryResponse inventoryResponse = inventoryService.getInventory(sku);
        return new ResponseEntity<>(inventoryResponse, HttpStatus.OK);
    }

    @PatchMapping("/stock")
    public ResponseEntity<InventoryResponse> addStock(
                        @RequestBody AddStockRequest  addStockRequest) {
        InventoryResponse inventoryResponse = inventoryService
                                                .addStock(addStockRequest);
        return new ResponseEntity<>(inventoryResponse, HttpStatus.OK);
    }

    @PatchMapping("/reservation")
    public ResponseEntity<InventoryResponse> updateReservation(
            @RequestBody ReserveStockRequest reserveStockRequest) {
        InventoryResponse inventoryResponse = inventoryService
                                    .createReservation(reserveStockRequest);
        return new ResponseEntity<>(inventoryResponse, HttpStatus.OK);
    }
}
