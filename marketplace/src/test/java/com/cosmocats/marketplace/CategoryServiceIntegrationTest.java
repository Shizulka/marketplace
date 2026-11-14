package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CategoryNotFoundException;
import com.cosmocats.marketplace.application.service.CategoryService;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSummary;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryServiceIntegrationTest extends AbstractPostgresTest {

    @Autowired private CategoryService categoryService;
    @Autowired private ProductJpaRepository productRepository;
    @PersistenceContext private EntityManager entityManager;

    @Test
    void createAndFindByIdShouldWork() {
        CategoryEntity created = categoryService.create("Toys", "Cosmic toys");

        assertNotNull(created.getId());
        assertEquals("Toys", categoryService.findById(created.getId()).getName());
    }

    @Test
    void findByIdShouldThrowForUnknownCategory() {
        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(999_999L));
    }

    @Test
    void findAllShouldBeSortedByName() {
        categoryService.create("Zeta", null);
        categoryService.create("Alpha", null);

        List<CategoryEntity> all = categoryService.findAll();

        assertEquals("Alpha", all.get(0).getName());
        assertEquals("Zeta", all.get(1).getName());
    }

    @Test
    void searchShouldMatchNamePartIgnoringCaseAndSortByName() {
        categoryService.create("Space Toys", null);
        categoryService.create("Cat toys", null);
        categoryService.create("Food", null);

        List<CategoryEntity> found = categoryService.search("TOYS");

        assertEquals(2, found.size());
        assertEquals("Cat toys", found.get(0).getName());
        assertEquals("Space Toys", found.get(1).getName());
    }

    @Test
    void updateShouldChangeNameAndDescription() {
        CategoryEntity created = categoryService.create("Old", "old");

        CategoryEntity updated = categoryService.update(created.getId(), "New", "new");

        assertEquals("New", updated.getName());
        assertEquals("new", updated.getDescription());
    }

    @Test
    void createShouldRejectDuplicateName() {
        categoryService.create("Toys", null);

        assertThrows(DataIntegrityViolationException.class,
                () -> categoryService.create("Toys", null));
    }

    @Test
    void deleteShouldRemoveEmptyCategory() {
        CategoryEntity created = categoryService.create("Temp", null);

        categoryService.delete(created.getId());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(created.getId()));
    }

    @Test
    void deleteShouldFailWhenCategoryHasProducts() {
        CategoryEntity category = categoryService.create("Busy", null);
        productRepository.saveAndFlush(
                new ProductEntity("Item", "d", BigDecimal.TEN, category, 1));
        // як у реальному запиті: нова сесія, у якій товар не завантажений у persistence context,
        // тож порушення FK приходить саме від БД
        entityManager.flush();
        entityManager.clear();

        assertThrows(DataIntegrityViolationException.class,
                () -> categoryService.delete(category.getId()));
    }

    @Test
    void findProductSummariesShouldReturnProjectionOrderedByPrice() {
        CategoryEntity category = categoryService.create("Toys", null);
        productRepository.save(new ProductEntity("Expensive", "d", new BigDecimal("30.00"), category, 1));
        productRepository.save(new ProductEntity("Cheap", "d", new BigDecimal("5.00"), category, 1));
        productRepository.flush();

        List<ProductSummary> summaries = categoryService.findProductSummaries(category.getId());

        assertEquals(2, summaries.size());
        assertEquals("Cheap", summaries.get(0).getName());
        assertEquals(0, new BigDecimal("5.00").compareTo(summaries.get(0).getPrice()));
        assertNotNull(summaries.get(0).getId());
        assertEquals("Expensive", summaries.get(1).getName());
    }

    @Test
    void findProductSummariesShouldThrowForUnknownCategory() {
        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.findProductSummaries(999_999L));
    }
}
