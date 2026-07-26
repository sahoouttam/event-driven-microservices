package com.event.driven.fulfillment.service.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.event.driven.fulfillment.service.config.AccountConfig;
import com.event.driven.fulfillment.service.dto.response.AddressResponse;
import com.event.driven.fulfillment.service.dto.response.CustomerResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountClient {
    
    private final AccountConfig accountConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public AccountClient(AccountConfig accountConfig, RestTemplate restTemplate) {
        this.accountConfig = accountConfig;
        this.restTemplate = restTemplate;
    }

    @Async("taskExecutor")
    public CompletableFuture<CustomerResponse> getCustomer(Long customerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("customerId", customerId);
            ResponseEntity<CustomerResponse> response = restTemplate.exchange(
                accountConfig.getAccountServiceUrl() + accountConfig.getGetCustomerUrl(), 
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<CustomerResponse>() {},
                params
            );
            return CompletableFuture.completedFuture(response.getBody());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<AddressResponse> getAddress(Long customerId, Long addressId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("customerId", customerId);
            params.put("addressId", addressId);
            ResponseEntity<AddressResponse> response = restTemplate.exchange(
                accountConfig.getAccountServiceUrl() + accountConfig.getGetAddressUrl(), 
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<AddressResponse>() {},
                params
            );
            return CompletableFuture.completedFuture(response.getBody());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Address not found with id: " + customerId);
        }
    }
}
