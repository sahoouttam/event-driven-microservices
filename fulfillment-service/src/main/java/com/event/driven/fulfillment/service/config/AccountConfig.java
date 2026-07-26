package com.event.driven.fulfillment.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class AccountConfig {
    
    @Value("${account.service.base.url}")
    private String accountServiceUrl;

    @Value("${account.service.get.customer.url}")
    private String getCustomerUrl;

    @Value("${account.service.get.address.url}")
    private String getAddressUrl;
}
