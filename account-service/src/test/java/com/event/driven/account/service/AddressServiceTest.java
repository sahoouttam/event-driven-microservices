package com.event.driven.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.event.driven.account.service.dto.request.CreateAddressRequest;
import com.event.driven.account.service.dto.request.CreateCustomerRequest;
import com.event.driven.account.service.dto.request.UpdateAddressRequest;
import com.event.driven.account.service.dto.response.AddressResponse;
import com.event.driven.account.service.dto.response.CustomerResponse;
import com.event.driven.account.service.enums.AddressType;
import com.event.driven.account.service.exception.AddressException;
import com.event.driven.account.service.service.AddressService;
import com.event.driven.account.service.service.CustomerService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AddressServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;

    CustomerResponse customerResponse;

    @BeforeEach
    void setup() {
        CreateCustomerRequest createCustomerRequest = CreateCustomerRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .build();
        customerResponse = customerService.createCustomer(createCustomerRequest);
    }

    @Test
    void shouldAddAddress() {
        CreateAddressRequest createAddressRequest = CreateAddressRequest.builder()
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .country("USA")
                .addressType(AddressType.HOME)
                .build();

        AddressResponse addressResponse = addressService.addAddress(
                customerResponse.getId(), createAddressRequest);
        assertNotNull(addressResponse.getId());
        assertEquals("123 Main St", addressResponse.getStreet());
        assertEquals("New York", addressResponse.getCity());
        assertEquals("NY", addressResponse.getState());
        assertEquals("USA", addressResponse.getCountry());
    }

    @Test
    void shouldGetAddress() {
        CreateAddressRequest createAddressRequest = CreateAddressRequest.builder()
                .street("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .zipCode("20001")
                .country("USA")
                .addressType(AddressType.SHIPPING)
                .build();

        AddressResponse addressCreated = addressService.addAddress(
                        customerResponse.getId(), createAddressRequest);
        AddressResponse addressFound = addressService.getAddress(
                        customerResponse.getId(), addressCreated.getId());

        assertEquals(addressCreated.getId(), addressFound.getId());
        assertEquals("456 Oak Ave", addressFound.getStreet());
        assertEquals("Los Angeles", addressFound.getCity());
        assertEquals("CA", addressFound.getState());
        assertEquals("20001", addressFound.getZipCode());
        assertEquals("USA", addressFound.getCountry());
    }

    @Test
    void shouldUpdateAddress() {
        CreateAddressRequest createAddressRequest = CreateAddressRequest.builder()
                .street("Old Street")
                .city("Old City")
                .state("OS")
                .zipCode("000000")
                .country("USA")
                .addressType(AddressType.OFFICE)
                .build();
        AddressResponse addressResponse = addressService.addAddress(
                customerResponse.getId(), createAddressRequest);

        UpdateAddressRequest updateAddressRequest = UpdateAddressRequest.builder()
                .street("New Street")
                .city("New City")
                .state("NS")
                .zipCode("999999")
                .country("Canada")
                .build();
        AddressResponse addressUpdated = addressService.updateAddress(
                customerResponse.getId(),
                addressResponse.getId(),
                updateAddressRequest);
        assertEquals("New Street", addressUpdated.getStreet());
        assertEquals("New City", addressUpdated.getCity());
        assertEquals("999999", addressUpdated.getZipCode());
        assertEquals("Canada", addressUpdated.getCountry());
    }

    @Test
    void shouldDeleteAddress() {
        CreateAddressRequest createAddressRequest = CreateAddressRequest.builder()
                .street("Delete Street")
                .city("Delete City")
                .state("DS")
                .zipCode("123456")
                .country("USA")
                .addressType(AddressType.HOME)
                .build();
        AddressResponse addressResponse = addressService.addAddress(
                customerResponse.getId(), createAddressRequest);

        addressService.deleteAddress(customerResponse.getId(), addressResponse.getId());

        assertThrows(AddressException.class, () -> addressService
                    .getAddress(customerResponse.getId(), addressResponse.getId()));
    }

    @Test
    void shouldThrowExceptionWhenAddressNotFound() {
        assertThrows(AddressException.class, () -> addressService
                    .getAddress(customerResponse.getId(), 1000L));
    }

    @Test
    void shouldSetDefaultAddress() {
        CreateAddressRequest createAddressRequest1 = CreateAddressRequest.builder()
                            .street("Street 1")
                            .city("City 1")
                            .state("S1")
                            .zipCode("11111")
                            .country("USA")
                            .addressType(AddressType.SHIPPING)
                            .build();
        AddressResponse addressResponse1 = addressService.addAddress(
                            customerResponse.getId(), createAddressRequest1);
        
        CreateAddressRequest createAddressRequest2 = CreateAddressRequest.builder()
                            .street("Street 2")
                            .city("City 2")
                            .state("S2")
                            .zipCode("22222")
                            .country("USA")
                            .addressType(AddressType.SHIPPING)
                            .build();
        AddressResponse addressResponse2 = addressService.addAddress(
                            customerResponse.getId(), createAddressRequest2);

        addressService.setDefaultAddress(
                customerResponse.getId(), addressResponse1.getId());

        AddressResponse defaultAddress = addressService.getAddress(
                    customerResponse.getId(), addressResponse1.getId());
        assertTrue(defaultAddress.isDefault());

        AddressResponse nonDefaultAddress = addressService.getAddress(
                    customerResponse.getId(), addressResponse2.getId());
        assertFalse(nonDefaultAddress.isDefault());
    }
}
