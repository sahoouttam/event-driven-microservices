package com.event.driven.account.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.account.service.dto.request.CreateCustomerRequest;
import com.event.driven.account.service.dto.request.UpdateCustomerRequest;
import com.event.driven.account.service.dto.response.CustomerResponse;
import com.event.driven.account.service.service.CustomerService;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody CreateCustomerRequest createCustomerRequest) {
        CustomerResponse customerResponse = customerService.createCustomer(createCustomerRequest);
        return new ResponseEntity<>(customerResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long customerId) {
        CustomerResponse customerResponse = customerService.getCustomer(customerId);
        return new ResponseEntity<>(customerResponse, HttpStatus.OK);
    }

    @GetMapping("/email{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        CustomerResponse customerResponse = customerService.getCustomerByEmail(email);
        return new ResponseEntity<>(customerResponse, HttpStatus.OK);
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
                            @PathVariable Long customerId,
                            @RequestBody UpdateCustomerRequest updateCustomerRequest) {
        CustomerResponse customerResponse = customerService.updateCustomer(
                                customerId, updateCustomerRequest);
        return new ResponseEntity<>(customerResponse, HttpStatus.OK);
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<Void> deactivateCustomer(@PathVariable Long customerId) {
        customerService.deactivateCustomer(customerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
