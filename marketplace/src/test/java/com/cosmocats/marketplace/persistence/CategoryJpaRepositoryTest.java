package com.cosmocats.marketplace.persistence;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.cosmocats.marketplace.support.PostgresDataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@PostgresDataJpaTest
class CategoryJpaRepositoryTest extends AbstractPostgresTest {

    @Autowired private CategoryJpaRepository repository;
    @PersistenceContext private EntityManager em;

    @Test
    void createShouldTakeIdFromSequence() {
        CategoryEntity first = repository.saveAndFlush(new CategoryEntity("Toys", "d"));
        CategoryEntity second = repository.saveAndFlush(new CategoryEntity("Food", "d"));

        assertNotNull(first.getId());
        assertTrue(second.getId() > first.getId());
    }

    @Test
    void readShouldReturnStoredRow() {
        Long id = repository.saveAndFlush(new CategoryEntity("Toys", "Cosmic toys")).getId();
        em.clear();

        CategoryEntity loaded = repository.findById(id).orElseThrow();

        assertEquals("Toys", loaded.getName());
        assertEquals("Cosmic toys", loaded.getDescription());
    }

    @Test
    void updateShouldPersistChanges() {
        Long id = repository.saveAndFlush(new CategoryEntity("Old", "old")).getId();
        em.clear();

        CategoryEntity loaded = repository.findById(id).orElseThrow();
        loaded.setName("New");
        loaded.setDescription("new");
        repository.flush();
        em.clear();

        CategoryEntity reloaded = repository.findById(id).orElseThrow();
        assertEquals("New", reloaded.getName());
        assertEquals("new", reloaded.getDescription());
    }

    @Test
    void deleteShouldRemoveRow() {
        Long id = repository.saveAndFlush(new CategoryEntity("Temp", null)).getId();

        repository.deleteById(id);
        repository.flush();

        assertFalse(repository.existsById(id));
    }

    @Test
    void duplicateNameShouldViolateUniqueConstraint() {
        repository.saveAndFlush(new CategoryEntity("Toys", null));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new CategoryEntity("Toys", "other")));
    }

    @Test
    void searchByNamePartShouldIgnoreCaseAndSortByName() {
        repository.save(new CategoryEntity("Space Toys", null));
        repository.save(new CategoryEntity("Cat toys", null));
        repository.save(new CategoryEntity("Food", null));

        List<CategoryEntity> found = repository.findByNameContainingIgnoreCaseOrderByNameAsc("TOYS");

        assertEquals(List.of("Cat toys", "Space Toys"),
                found.stream().map(CategoryEntity::getName).toList());
    }
}
