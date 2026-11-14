package com.cosmocats.marketplace.persistence;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSummary;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.cosmocats.marketplace.support.PostgresDataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@PostgresDataJpaTest
class ProductJpaRepositoryTest extends AbstractPostgresTest {

    @Autowired private ProductJpaRepository repository;
    @Autowired private CategoryJpaRepository categoryRepository;
    @PersistenceContext private EntityManager em;

    private CategoryEntity toys;

    @BeforeEach
    void setUp() {
        toys = categoryRepository.saveAndFlush(new CategoryEntity("Toys", null));
    }

    private ProductEntity product(String name, String price, CategoryEntity category) {
        return new ProductEntity(name, "desc", new BigDecimal(price), category, 5);
    }

    @Test
    void createAndReadShouldRoundTripAllFields() {
        UUID id = repository.saveAndFlush(product("Yarn", "19.99", toys)).getId();
        em.clear();

        ProductEntity loaded = repository.findById(id).orElseThrow();

        assertEquals("Yarn", loaded.getName());
        assertEquals("desc", loaded.getDescription());
        assertEquals(0, new BigDecimal("19.99").compareTo(loaded.getPrice()));
        assertEquals(5, loaded.getStockQuantity());
        assertEquals(toys.getId(), loaded.getCategory().getId());
        assertNotNull(loaded.getCreatedAt());
    }

    @Test
    void updateShouldPersistChanges() {
        UUID id = repository.saveAndFlush(product("Old", "1.00", toys)).getId();
        em.clear();

        ProductEntity loaded = repository.findById(id).orElseThrow();
        loaded.setName("New");
        loaded.setPrice(new BigDecimal("2.50"));
        loaded.setStockQuantity(9);
        repository.flush();
        em.clear();

        ProductEntity reloaded = repository.findById(id).orElseThrow();
        assertEquals("New", reloaded.getName());
        assertEquals(0, new BigDecimal("2.50").compareTo(reloaded.getPrice()));
        assertEquals(9, reloaded.getStockQuantity());
    }

    @Test
    void deleteShouldRemoveRow() {
        UUID id = repository.saveAndFlush(product("Gone", "1.00", toys)).getId();

        repository.deleteById(id);
        repository.flush();

        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void sameNameInSameCategoryShouldViolateUniqueConstraint() {
        repository.saveAndFlush(product("Yarn", "1.00", toys));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(product("Yarn", "2.00", toys)));
    }

    @Test
    void sameNameInAnotherCategoryShouldBeAllowed() {
        CategoryEntity food = categoryRepository.saveAndFlush(new CategoryEntity("Food", null));
        repository.saveAndFlush(product("Special", "1.00", toys));

        assertDoesNotThrow(() -> repository.saveAndFlush(product("Special", "1.00", food)));
    }

    @Test
    void priceBelowMinimumShouldViolateCheckConstraint() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(product("Free", "0.00", toys)));
    }

    @Test
    void negativeStockShouldViolateCheckConstraint() {
        ProductEntity negative = new ProductEntity("Minus", "d", BigDecimal.ONE, toys, -1);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(negative));
    }

    @Test
    void summariesShouldBeFilteredByCategoryAndOrderedByPrice() {
        CategoryEntity food = categoryRepository.saveAndFlush(new CategoryEntity("Food", null));
        repository.save(product("Expensive", "30.00", toys));
        repository.save(product("Cheap", "5.00", toys));
        repository.save(product("Milk", "1.00", food));
        repository.flush();

        List<ProductSummary> summaries = repository.findByCategoryIdOrderByPriceAsc(toys.getId());

        assertEquals(List.of("Cheap", "Expensive"),
                summaries.stream().map(ProductSummary::getName).toList());
        assertEquals(0, new BigDecimal("5.00").compareTo(summaries.get(0).getPrice()));
    }

    @Test
    void findByIdForUpdateShouldReturnLockedRowOrEmpty() {
        UUID id = repository.saveAndFlush(product("Locked", "1.00", toys)).getId();

        assertTrue(repository.findByIdForUpdate(id).isPresent());
        assertTrue(repository.findByIdForUpdate(UUID.randomUUID()).isEmpty());
    }
}
