package com.event.driven.order.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class StockConfig {
    

    @Value("${stock.service.base.url}")
    private String stockServiceUrl;

    @Value("${stock.service.get.product.url}")
    private String getProductUrl;
}
