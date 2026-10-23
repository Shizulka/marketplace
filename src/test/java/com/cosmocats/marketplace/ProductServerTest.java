package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import com.cosmocats.marketplace.application.service.ProductService;
import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class ProductServerTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    private Product createProduct(UUID id, String name) {
        return new Product(
                id,
                name,
                "Product description",
                new BigDecimal("19.99"),
                1L,
                10
        );
    }

    private List<Product> createProducts(int count) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            products.add(createProduct(
                    UUID.randomUUID(),
                    "Product " + (i + 1)
            ));
        }

        return products;
    }

    // CREATE

    @Test
    void createShouldSaveAndReturnProduct() {
        Product product = createProduct(null, "New product");
        Product savedProduct = createProduct(UUID.randomUUID(), "New product");

        when(productRepository.save(product)).thenReturn(savedProduct);

        Product result = productService.create(product);

        assertNotNull(result);
        assertEquals(savedProduct.getId(), result.getId());
        assertEquals("New product", result.getName());

        verify(productRepository).save(product);
    }

    // FIND ALL / PAGINATION

    @Test
    void findAllShouldReturnFirstPage() {
        List<Product> products = createProducts(5);

        when(productRepository.findAll()).thenReturn(products);

        PagedResponse<Product> result = productService.findAll(0, 2);

        assertEquals(0, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5L, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertEquals("Product 1", result.getContent().get(0).getName());
        assertEquals("Product 2", result.getContent().get(1).getName());
    }

    @Test
    void findAllShouldReturnSecondPage() {
        List<Product> products = createProducts(5);

        when(productRepository.findAll()).thenReturn(products);

        PagedResponse<Product> result = productService.findAll(1, 2);

        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5L, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertEquals("Product 3", result.getContent().get(0).getName());
        assertEquals("Product 4", result.getContent().get(1).getName());
    }

    @Test
    void findAllShouldReturnLastPartialPage() {
        List<Product> products = createProducts(5);

        when(productRepository.findAll()).thenReturn(products);

        PagedResponse<Product> result = productService.findAll(2, 2);

        assertEquals(2, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5L, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        assertEquals("Product 5", result.getContent().get(0).getName());
    }

    @Test
    void findAllShouldReturnEmptyContentWhenPageIsOutOfRange() {
        List<Product> products = createProducts(5);

        when(productRepository.findAll()).thenReturn(products);

        PagedResponse<Product> result = productService.findAll(3, 2);

        assertTrue(result.getContent().isEmpty());
        assertEquals(3, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5L, result.getTotalElements());
    }

    @Test
    void findAllShouldReturnEmptyContentWhenNoProductsExist() {
        when(productRepository.findAll()).thenReturn(List.of());

        PagedResponse<Product> result = productService.findAll(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void findAllShouldReturnAllProductsWhenPageSizeIsLargeEnough() {
        List<Product> products = createProducts(3);

        when(productRepository.findAll()).thenReturn(products);

        PagedResponse<Product> result = productService.findAll(0, 10);

        assertEquals(3, result.getContent().size());
        assertEquals(3L, result.getTotalElements());
    }

    @Test
    void findAllShouldReturnEmptyContentWhenPageStartsAtTotal() {
        List<Product> products = createProducts(4);

        when(productRepository.findAll()).thenReturn(products);

        PagedResponse<Product> result = productService.findAll(2, 2);

        assertTrue(result.getContent().isEmpty());
        assertEquals(4L, result.getTotalElements());
    }

    // FIND BY ID

    @Test
    void findByIdShouldReturnProductWhenFound() {
        UUID id = UUID.randomUUID();
        Product product = createProduct(id, "Existing product");

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Existing product", result.getName());

        verify(productRepository).findById(id);
    }

    @Test
    void findByIdShouldThrowExceptionWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById(id)
        );

        verify(productRepository).findById(id);
    }

    // UPDATE

    @Test
    void updateShouldReturnUpdatedProductWhenFound() {
        UUID id = UUID.randomUUID();

        Product updatedProduct = createProduct(null, "Updated product");
        Product savedProduct = createProduct(id, "Updated product");

        when(productRepository.update(id, updatedProduct))
                .thenReturn(Optional.of(savedProduct));

        Product result = productService.update(id, updatedProduct);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Updated product", result.getName());

        verify(productRepository).update(id, updatedProduct);
    }

    @Test
    void updateShouldThrowExceptionWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        Product updatedProduct = createProduct(null, "Updated product");

        when(productRepository.update(id, updatedProduct))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.update(id, updatedProduct)
        );

        verify(productRepository).update(id, updatedProduct);
    }

    // DELETE

    @Test
    void deleteShouldDeleteProductWhenFound() {
        UUID id = UUID.randomUUID();

        when(productRepository.deleteById(id)).thenReturn(true);

        assertDoesNotThrow(() -> productService.delete(id));

        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteShouldThrowExceptionWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(productRepository.deleteById(id)).thenReturn(false);

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.delete(id)
        );

        verify(productRepository).deleteById(id);
    }
}
