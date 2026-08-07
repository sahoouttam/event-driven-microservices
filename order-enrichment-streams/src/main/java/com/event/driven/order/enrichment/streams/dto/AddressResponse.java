package com.event.driven.order.enrichment.streams.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressResponse {
    
    private Long addressId;
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private LocalDateTime createdAt;
}
