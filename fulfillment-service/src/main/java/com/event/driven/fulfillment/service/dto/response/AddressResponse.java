package com.event.driven.fulfillment.service.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    
    private Long addressId;
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private LocalDateTime createdAt;
}
