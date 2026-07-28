package com.event.driven.account.service.dto.request;

import com.event.driven.account.service.enums.AddressType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {
    
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private AddressType addressType;

}
