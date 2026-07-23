package com.event.driven.payment.service.dto.response;

import com.event.driven.payment.service.enums.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponse {
    
    private Long id;

    private TransactionStatus paymentAction;

    private String transactionReference; 

    private String pspReference;
}
