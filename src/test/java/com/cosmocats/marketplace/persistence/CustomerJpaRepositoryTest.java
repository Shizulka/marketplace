package com.cosmocats.marketplace.persistence;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CustomerJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.cosmocats.marketplace.support.PostgresDataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@PostgresDataJpaTest
class CustomerJpaRepositoryTest extends AbstractPostgresTest {

    @Autowired private CustomerJpaRepository repository;
    @PersistenceContext private EntityManager em;

    @Test
    void createReadAndDeleteShouldWork() {
        UUID id = repository.saveAndFlush(new CustomerEntity("cat@cosmo.cats", "Space Cat")).getId();
        em.clear();

        CustomerEntity loaded = repository.findById(id).orElseThrow();
        assertEquals("cat@cosmo.cats", loaded.getEmail());
        assertEquals("Space Cat", loaded.getFullName());
        assertNotNull(loaded.getCreatedAt());

        repository.delete(loaded);
        repository.flush();
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void duplicateEmailShouldViolateUniqueConstraint() {
        repository.saveAndFlush(new CustomerEntity("same@cosmo.cats", "First"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new CustomerEntity("same@cosmo.cats", "Second")));
    }
}
