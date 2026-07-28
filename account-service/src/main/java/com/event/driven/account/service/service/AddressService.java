package com.event.driven.account.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.account.service.dto.request.CreateAddressRequest;
import com.event.driven.account.service.dto.request.UpdateAddressRequest;
import com.event.driven.account.service.dto.response.AddressResponse;
import com.event.driven.account.service.entity.Address;
import com.event.driven.account.service.entity.Customer;
import com.event.driven.account.service.exception.AddressException;
import com.event.driven.account.service.repository.AddressRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddressService {
    
    private final AddressRepository addressRepository;
    private final CustomerService customerService;

    @Autowired
    public AddressService(AddressRepository addressRepository, CustomerService customerService) {
        this.addressRepository = addressRepository;
        this.customerService = customerService;
    }

    public AddressResponse addAddress(Long customerId, CreateAddressRequest createAddressRequest) {
        Customer customer = customerService.findById(customerId);
        Address address = Address.builder()
                    .city(createAddressRequest.getCity())
                    .street(createAddressRequest.getStreet())
                    .state(createAddressRequest.getState())
                    .country(createAddressRequest.getCountry())
                    .zipCode(createAddressRequest.getZipCode())
                    .isDefault(false)
                    .addressType(createAddressRequest.getAddressType())
                    .customer(customer)
                    .build();
        Address savedAddress = addressRepository.save(address);
        return toResponse(savedAddress);
    }

    public AddressResponse updateAddress(Long customerId, Long addressId, 
                                      UpdateAddressRequest updateAddressRequest) {
        Address address = getAddressByCustomer(customerId, addressId);
        if (updateAddressRequest.getCity() != null) {
            address.setCity(updateAddressRequest.getCity());
        }
        if (updateAddressRequest.getStreet() != null) {
            address.setStreet(updateAddressRequest.getStreet());
        }
        if (updateAddressRequest.getState() != null) {
            address.setState(updateAddressRequest.getState());
        }
        if (updateAddressRequest.getCountry() != null) {
            address.setCountry(updateAddressRequest.getCountry());
        }
        if (updateAddressRequest.getZipCode() != null) {
            address.setZipCode(updateAddressRequest.getZipCode());
        }
        
        if (updateAddressRequest.getAddressType() != null) {
            address.setAddressType(updateAddressRequest.getAddressType());
        }
        Address savedAddress = addressRepository.save(address);
        return toResponse(savedAddress);
    }

    public AddressResponse getAddress(Long customerId, Long addressId) {
        Address address = getAddressByCustomer(customerId, addressId);
        return toResponse(address);
    }

    public List<AddressResponse> getAddresses(Long customerId) {
        Customer customer = customerService.findById(customerId);
        return addressRepository.findByCustomer(customer)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
    }

    public void setDefaultAddress(Long customerId, Long addressId) {
        Address address = getAddressByCustomer(customerId, addressId);
        address.setDefault(true);
        addressRepository.save(address);
    }

    public void deleteAddress(Long customerId, Long addressId) {
        Address address = getAddressByCustomer(customerId, addressId);
        addressRepository.delete(address);
    }

    public Address getAddressByCustomer(Long customerId, Long addressId) {
        Customer customer = customerService.findById(customerId);
        return addressRepository.findByIdAndCustomer(addressId, customer)
                .orElseThrow(() -> new AddressException("Address not found"));
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                    .id(address.getId())
                    .street(address.getStreet())
                    .city(address.getCity())
                    .state(address.getState())
                    .country(address.getCountry())
                    .zipCode(address.getZipCode())
                    .isDefault(address.isDefault())
                    .createdAt(address.getCreatedAt())
                    .build();
    }
}
