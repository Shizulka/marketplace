package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CategoryNotFoundException;
import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductRepositoryIntegrationTest extends AbstractPostgresTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryJpaRepository categoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = categoryRepository.save(new CategoryEntity("Toys", "Toys")).getId();
    }

    private Product newProduct(String name) {
        return new Product(null, name, "desc", new BigDecimal("9.99"), categoryId, 5);
    }

    @Test
    void saveShouldGenerateIdAndPersistFields() {
        Product saved = productRepository.save(newProduct("Yarn"));

        assertNotNull(saved.getId());
        assertEquals("Yarn", saved.getName());
        assertEquals(categoryId, saved.getCategoryId());
        assertEquals(5, saved.getStockQuantity());
        assertEquals(0, new BigDecimal("9.99").compareTo(saved.getPrice()));
    }

    @Test
    void saveShouldRejectUnknownCategory() {
        Product product = new Product(null, "Yarn", "d", BigDecimal.ONE, 999_999L, 1);

        assertThrows(CategoryNotFoundException.class, () -> productRepository.save(product));
    }

    @Test
    void saveShouldRejectDuplicateNameInSameCategory() {
        productRepository.save(newProduct("Yarn"));

        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.save(newProduct("Yarn")));
    }

    @Test
    void findAllAndFindByIdShouldReturnSavedProducts() {
        Product first = productRepository.save(newProduct("A"));
        productRepository.save(newProduct("B"));

        List<Product> all = productRepository.findAll();

        assertEquals(2, all.size());
        assertEquals(Optional.of("A"), productRepository.findById(first.getId()).map(Product::getName));
        assertTrue(productRepository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void updateShouldChangeFieldsOrReturnEmpty() {
        Product saved = productRepository.save(newProduct("Old"));
        Long otherCategory = categoryRepository.save(new CategoryEntity("Food", null)).getId();

        Optional<Product> updated = productRepository.update(saved.getId(),
                new Product(null, "New", "new desc", new BigDecimal("1.50"), otherCategory, 7));

        assertTrue(updated.isPresent());
        assertEquals("New", updated.get().getName());
        assertEquals(otherCategory, updated.get().getCategoryId());
        assertEquals(7, updated.get().getStockQuantity());
        assertTrue(productRepository.update(UUID.randomUUID(), newProduct("X")).isEmpty());
    }

    @Test
    void deleteByIdShouldReportWhetherProductExisted() {
        Product saved = productRepository.save(newProduct("Gone"));

        assertTrue(productRepository.deleteById(saved.getId()));
        assertFalse(productRepository.deleteById(saved.getId()));
        assertTrue(productRepository.findById(saved.getId()).isEmpty());
    }
}
