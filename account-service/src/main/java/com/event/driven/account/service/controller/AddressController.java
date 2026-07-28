package com.event.driven.account.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.account.service.dto.request.CreateAddressRequest;
import com.event.driven.account.service.dto.request.UpdateAddressRequest;
import com.event.driven.account.service.dto.response.AddressResponse;
import com.event.driven.account.service.service.AddressService;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
public class AddressController {
    
    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
                        @PathVariable Long customerId,
                        @RequestBody CreateAddressRequest createAddressRequest) {
        AddressResponse addressResponse = addressService.addAddress(
                                        customerId, createAddressRequest);
        return new ResponseEntity<>(addressResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
                                            @PathVariable Long customerId) {
        List<AddressResponse> addressResponses = addressService.getAddresses(customerId);
        return new ResponseEntity<>(addressResponses, HttpStatus.OK);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
                                    @PathVariable Long customerId,
                                    @PathVariable Long addressId) {
        AddressResponse addressResponse = addressService.getAddress(customerId, addressId);
        return new ResponseEntity<>(addressResponse, HttpStatus.OK);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
                        @PathVariable Long customerId,
                        @PathVariable Long addressId,
                        @RequestBody UpdateAddressRequest updateAddressRequest) {
        AddressResponse addressResponse = addressService.updateAddress(
                                customerId, addressId, updateAddressRequest);
        return new ResponseEntity<>(addressResponse, HttpStatus.OK);
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
                        @PathVariable Long customerId, @PathVariable Long addressId) {
        addressService.setDefaultAddress(customerId, addressId);;
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
                        @PathVariable Long customerId, @PathVariable Long addressId) {
        addressService.deleteAddress(customerId, addressId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
