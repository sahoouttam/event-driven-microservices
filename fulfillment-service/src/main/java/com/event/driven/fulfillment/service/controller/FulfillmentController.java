package com.event.driven.fulfillment.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.fulfillment.service.dto.response.FulfillmentResponse;
import com.event.driven.fulfillment.service.service.FulfillmentService;

@RestController
@RequestMapping("/api/v1/fulfillment")
public class FulfillmentController {
    
    private final FulfillmentService fulfillmentService;

    @Autowired
    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<FulfillmentResponse> getFulfillmentByOrder(@PathVariable Long orderId) {
        FulfillmentResponse fulfillmentResponse = fulfillmentService.getFulfillmentByOrder(orderId);
        return new ResponseEntity<>(fulfillmentResponse, HttpStatus.OK);
    }

    @GetMapping("/{fulfillmentId}")
    public ResponseEntity<FulfillmentResponse> getFulfillment(@PathVariable Long fulfillmentId) {
        FulfillmentResponse fulfillmentResponse = fulfillmentService.getFulfillment(fulfillmentId);
        return new ResponseEntity<>(fulfillmentResponse, HttpStatus.OK);
    }

    @PostMapping("/{fulfillmentId}/pick")
    public ResponseEntity<FulfillmentResponse> startPicking(@PathVariable Long fulfillmentId) {
        FulfillmentResponse fulfillmentResponse = fulfillmentService.startPicking(fulfillmentId);
        return new ResponseEntity<>(fulfillmentResponse, HttpStatus.OK);
    }

    @PostMapping("/{fulfillmentId}/pack")
    public ResponseEntity<FulfillmentResponse> markPacked(@PathVariable Long fulfillmentId) {
        FulfillmentResponse fulfillmentResponse = fulfillmentService.markPacked(fulfillmentId);
        return new ResponseEntity<>(fulfillmentResponse, HttpStatus.OK);
    }

    @PostMapping("/{fulfillmentId}/ship")
    public ResponseEntity<FulfillmentResponse> shipOrder(@PathVariable Long fulfillmentId) {
        FulfillmentResponse fulfillmentResponse = fulfillmentService.shipOrder(fulfillmentId);
        return new ResponseEntity<>(fulfillmentResponse, HttpStatus.OK);
    }

    @PostMapping("/{fulfillmentId}/deliver")
    public ResponseEntity<FulfillmentResponse> markedDelivered(@PathVariable Long fulfillmentId) {
        FulfillmentResponse fulfillmentResponse = fulfillmentService.markDelivered(fulfillmentId);
        return new ResponseEntity<>(fulfillmentResponse, HttpStatus.OK);
    }
}
