package com.event.driven.order.enrichment.streams.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResponse {
    
    private Long id;

    private String name;

    private String email;

    private String phone;
}
