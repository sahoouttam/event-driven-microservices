package com.event.driven.account.service.dto.response;

import com.event.driven.account.service.enums.CustomerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    
    private Long id;

    private String name;

    private String email;

    private String phone;

    private CustomerStatus customerStatus;
}
