package com.event.driven.account.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.account.service.entity.Address;
import com.event.driven.account.service.entity.Customer;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByCustomer(Customer customer);

    Optional<Address> findByIdAndCustomer(Long id, Customer customer);
}
