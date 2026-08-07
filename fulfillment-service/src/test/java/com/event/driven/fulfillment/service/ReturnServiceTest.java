package com.event.driven.fulfillment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.event.driven.fulfillment.service.dto.request.CreateReturnRequest;
import com.event.driven.fulfillment.service.dto.request.ReturnItemRequest;
import com.event.driven.fulfillment.service.dto.response.FulfillmentResponse;
import com.event.driven.fulfillment.service.dto.response.ReturnResponse;
import com.event.driven.fulfillment.service.enums.FulfillmentStatus;
import com.event.driven.fulfillment.service.enums.ReturnStatus;
import com.event.driven.fulfillment.service.repository.ReturnItemRepository;
import com.event.driven.fulfillment.service.repository.ReturnRepository;
import com.event.driven.fulfillment.service.service.FulfillmentService;
import com.event.driven.fulfillment.service.service.ReturnService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReturnServiceTest {
    
    @Autowired
    private ReturnService returnService;

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private ReturnItemRepository returnItemRepository;

    @MockitoBean
    private FulfillmentService fulfillmentService;

    @BeforeEach
    void setup() {
        returnRepository.deleteAll();
        returnItemRepository.deleteAll();
    }

    @Test
    void shouldInitiateReturn() {
        FulfillmentResponse fulfillmentResponse = FulfillmentResponse.builder()
                                .id(1L)
                                .orderId(2L)
                                .customerId(3L)
                                .fulfillmentStatus(FulfillmentStatus.DELIVERED)
                                .build();
        when(fulfillmentService.getFulfillmentByOrder(2L)).thenReturn(fulfillmentResponse);

        List<ReturnItemRequest> returnItemRequests = Arrays.asList(
            new ReturnItemRequest(10L, "PRODUCT 1", 1)
        );

        CreateReturnRequest createReturnRequest = CreateReturnRequest.builder()
                                .orderId(2L)
                                .returnItemRequests(returnItemRequests)
                                .build();
        ReturnResponse returnResponse = returnService.initiateReturn(createReturnRequest);

        assertNotNull(returnResponse.getId());
        assertEquals(ReturnStatus.INITIATED, returnResponse.getReturnStatus());
    }

    @Test
    void shouldCompleteReturn() {
        FulfillmentResponse fulfillmentResponse = FulfillmentResponse.builder()
                                .id(1L)
                                .orderId(2L)
                                .customerId(3L)
                                .fulfillmentStatus(FulfillmentStatus.DELIVERED)
                                .build();
        when(fulfillmentService.getFulfillmentByOrder(2L)).thenReturn(fulfillmentResponse);

        List<ReturnItemRequest> returnItemRequests = Arrays.asList(
            new ReturnItemRequest(10L, "PRODUCT 1", 1)
        );

        CreateReturnRequest createReturnRequest = CreateReturnRequest.builder()
                                .orderId(2L)
                                .returnItemRequests(returnItemRequests)
                                .build();
        ReturnResponse returnInitiated = returnService.initiateReturn(createReturnRequest);

        ReturnResponse returnApproved = returnService.approveReturn(returnInitiated.getId());
        assertEquals(ReturnStatus.APPROVED, returnApproved.getReturnStatus());

        ReturnResponse returnReceived = returnService.markReceived(returnInitiated.getId());
        assertEquals(ReturnStatus.RECEIVED, returnReceived.getReturnStatus());
        
        ReturnResponse returnCompleted = returnService.markCompleted(returnInitiated.getId());
        assertEquals(ReturnStatus.COMPLETED, returnCompleted.getReturnStatus());
    }
}
