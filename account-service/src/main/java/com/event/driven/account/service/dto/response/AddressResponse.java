package com.event.driven.account.service.dto.response;

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

    private Long id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
