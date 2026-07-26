package com.event.driven.account.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.account.service.dto.request.CreateCustomerRequest;
import com.event.driven.account.service.dto.request.UpdateCustomerRequest;
import com.event.driven.account.service.dto.response.CustomerResponse;
import com.event.driven.account.service.entity.Customer;
import com.event.driven.account.service.enums.CustomerStatus;
import com.event.driven.account.service.exception.CustomerException;
import com.event.driven.account.service.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerService {
    
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest createCustomerRequest) {
        Customer customer = Customer.builder()
                    .name(createCustomerRequest.getName())
                    .email(createCustomerRequest.getEmail())
                    .phone(createCustomerRequest.getPhone())
                    .customerStatus(CustomerStatus.ACTIVE)
                    .build();
        Customer savedCustomer = saveCustomer(customer);
        log.info("customer created with id {}", savedCustomer.getId());
        return toResponse(savedCustomer);
    }

    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest updateCustomerRequest) {
        Customer customer = findById(id);
        customer.setName(updateCustomerRequest.getName());
        customer.setEmail(updateCustomerRequest.getEmail());
        customer.setPhone(updateCustomerRequest.getPhone());
        Customer savedCustomer = saveCustomer(customer);
        log.info("customer updated with id {}", savedCustomer.getId());
        return toResponse(savedCustomer);
    }

    public CustomerResponse getCustomer(Long id) {
        Customer customer = findById(id);
        return toResponse(customer);
                    
    }

    public CustomerResponse getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                    .map(this::toResponse)
                    .orElseThrow(() -> new CustomerException(
                                    "customer not found"));
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
    }

    public void deactivateCustomer(Long id) {
        Customer customer = findById(id);
        customer.setCustomerStatus(CustomerStatus.SUSPENDED);
        customerRepository.save(customer);
        log.info("customer deactivated with id {}", id);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                    .orElseThrow(() -> new CustomerException(
                                    "customer not found"));
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                    .id(customer.getId())
                    .name(customer.getName())
                    .email(customer.getEmail())
                    .phone(customer.getPhone())
                    .customerStatus(customer.getCustomerStatus())
                    .build();
    }
}
