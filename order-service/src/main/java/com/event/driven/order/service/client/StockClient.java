package com.event.driven.order.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.event.driven.order.service.config.StockConfig;
import com.event.driven.order.service.dto.response.ProductResponse;
import com.event.driven.order.service.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StockClient {
    
    private final StockConfig stockConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public StockClient(StockConfig stockConfig, RestTemplate restTemplate) {
        this.stockConfig = stockConfig;
        this.restTemplate = restTemplate;
    }
    
    public ProductResponse getProductBySku(String sku) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.exchange(
                stockConfig.getStockServiceUrl() + stockConfig.getStockServiceUrl(), 
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<ProductResponse>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Product not found with SKU: " + sku);
        }
    }
}
