package com.event.driven.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.account.service.dto.request.CreateCustomerRequest;
import com.event.driven.account.service.dto.request.UpdateCustomerRequest;
import com.event.driven.account.service.dto.response.CustomerResponse;
import com.event.driven.account.service.enums.CustomerStatus;
import com.event.driven.account.service.exception.CustomerException;
import com.event.driven.account.service.service.CustomerService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class CustomerServiceTest {
    
    @Autowired
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {
        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .phone("1234567890")
                            .build();
        CustomerResponse customerResponse = customerService.createCustomer(createCustomerRequest);

        assertNotNull(customerResponse.getId());
        assertEquals("john@example.com", customerResponse.getEmail());
    }

    @Test
    void shouldFindCustomerById() {
        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .phone("1234567890")
                            .build();
        
        CustomerResponse customerCreated = customerService.createCustomer(createCustomerRequest);
        CustomerResponse customerFound = customerService.getCustomer(customerCreated.getId());
        
        assertEquals(customerCreated.getId(), customerFound.getId());
        assertEquals("john@example.com", customerFound.getEmail());
        assertEquals("1234567890", customerFound.getPhone());
    }

    @Test
    void shouldUpdateCustomer() {
        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .phone("1234567890")
                            .build();
        CustomerResponse customerCreated = customerService.createCustomer(createCustomerRequest);
        
        UpdateCustomerRequest updateCustomerRequest = UpdateCustomerRequest.builder()
                            .name("John Doh")
                            .email("john_new@example.com")
                            .phone("1234567891")
                            .build();
        CustomerResponse customerUpdated = customerService.updateCustomer(
                                    customerCreated.getId(), updateCustomerRequest);
        
        assertEquals("John Doh", customerUpdated.getName());                            
        assertEquals("john_new@example.com", customerUpdated.getEmail());
        assertEquals("1234567891", customerUpdated.getPhone());
    }

    @Test
    void shouldFindCustomerByEmail() {
        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .phone("1234567890")
                            .build();
        CustomerResponse customerCreated = customerService.createCustomer(createCustomerRequest);
        
        CustomerResponse customerFound = customerService.getCustomerByEmail(customerCreated.getEmail());
        
        assertEquals("John Doe", customerFound.getName());                            
        assertEquals("john@example.com", customerFound.getEmail());
        assertEquals("1234567890", customerFound.getPhone());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        assertThrows(CustomerException.class, () -> customerService.getCustomer(999L));
    }

    @Test
    void shouldDeactivateCustomer() {
        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .phone("1234567890")
                            .build();
        CustomerResponse customerResponse = customerService.createCustomer(createCustomerRequest);
        
        customerService.deactivateCustomer(customerResponse.getId());

        CustomerResponse customerFound = customerService.getCustomer(customerResponse.getId());
        assertEquals(CustomerStatus.INACTIVE, customerFound.getCustomerStatus());
    }
}
