package com.event.driven.fulfillment.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.fulfillment.service.dto.request.CreateReturnRequest;
import com.event.driven.fulfillment.service.dto.response.ReturnResponse;
import com.event.driven.fulfillment.service.service.ReturnService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {
    
    private final ReturnService returnService;

    @Autowired
    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    public ResponseEntity<ReturnResponse> initiateReturn(@RequestBody CreateReturnRequest createReturnRequest) {
        ReturnResponse returnResponse = returnService.initiateReturn(createReturnRequest);
        return new ResponseEntity<>(returnResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponse> getReturn(@PathVariable Long returnId) {
        ReturnResponse returnResponse = returnService.getReturn(returnId);
        return new ResponseEntity<>(returnResponse, HttpStatus.OK);
    }

    @PostMapping("/{returnId}/approve")
    public ResponseEntity<ReturnResponse> approveReturn(@PathVariable Long returnId) {
        ReturnResponse returnResponse = returnService.approveReturn(returnId);
        return new ResponseEntity<>(returnResponse, HttpStatus.OK);
    }

    @PostMapping("/{returnId}/reject")
    public ResponseEntity<ReturnResponse> rejectReturn(@PathVariable Long returnId) {
        ReturnResponse returnResponse = returnService.rejectReturn(returnId);
        return new ResponseEntity<>(returnResponse, HttpStatus.OK);
    }

    @PostMapping("/{returnId}/receive")
    public ResponseEntity<ReturnResponse> markReceived(@PathVariable Long returnId) {
        ReturnResponse returnResponse = returnService.markReceived(returnId);
        return new ResponseEntity<>(returnResponse, HttpStatus.OK);
    }

    @PostMapping("/{returnId}/complete")
    public ResponseEntity<ReturnResponse> markCompleted(@PathVariable Long returnId) {
        ReturnResponse returnResponse = returnService.markCompleted(returnId);
        return new ResponseEntity<>(returnResponse, HttpStatus.OK);
    }
}
