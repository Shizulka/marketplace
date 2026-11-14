package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.application.exception.CustomerNotFoundException;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CustomerJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private final CustomerJpaRepository customerRepository;

    public CustomerService(CustomerJpaRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerEntity create(String email, String fullName) {
        return customerRepository.saveAndFlush(new CustomerEntity(email, fullName));
    }

    @Transactional(readOnly = true)
    public List<CustomerEntity> findAll() {
        return customerRepository.findAll(Sort.by("createdAt", "id"));
    }

    @Transactional(readOnly = true)
    public CustomerEntity findById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public void delete(UUID id) {
        CustomerEntity customer = findById(id);
        customerRepository.delete(customer);
        customerRepository.flush();
    }
}
