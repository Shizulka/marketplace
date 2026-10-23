package com.cosmocats.marketplace;


import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.application.service.ProductService;
import com.cosmocats.marketplace.presentation.controller.ProductController;
import com.cosmocats.marketplace.presentation.dto.ProductRequest;
import com.cosmocats.marketplace.presentation.dto.ProductResponse;
import com.cosmocats.marketplace.presentation.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;


    @Test
    void createProductShouldReturnSuccessForValidRequest()
            throws Exception {
        Product product = new Product(
                UUID.randomUUID(),
                "Cosmic Cat Toy",
                "A toy for cosmic cats",
                new BigDecimal("19.99"),
                1L,
                10
        );
        product.setId(UUID.randomUUID());

        ProductResponse response = new ProductResponse();

        when(productMapper.toDomain(any(ProductRequest.class)))
                .thenReturn(product);

        when(productService.create(any(Product.class)))
                .thenReturn(product);

        when(productMapper.toDto(any(Product.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Cosmic Cat Toy",
                              "description": "A toy for cosmic cats",
                              "price": 19.99,
                              "categoryId": 1,
                              "stockQuantity": 10
                            }
                            """))
                .andExpect(status().isCreated());

        verify(productMapper).toDomain(any(ProductRequest.class));
        verify(productService).create(any(Product.class));
        verify(productMapper).toDto(any(Product.class));
    }

    @Test
    void createProductShouldRejectBlankName() throws Exception {
        assertInvalidRequest("""
                {
                  "name": "   ",
                  "description": "A toy",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectNameShorterThanThreeCharacters()
            throws Exception {
        assertInvalidRequest("""
                {
                  "name": "AB",
                  "description": "A toy",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectNameLongerThanOneHundredCharacters()
            throws Exception {
        String longName = "A".repeat(101);

        assertInvalidRequest("""
                {
                  "name": "%s",
                  "description": "A toy",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """.formatted(longName));
    }

    @Test
    void createProductShouldRejectDescriptionLongerThanFiveHundredCharacters()
            throws Exception {
        String longDescription = "A".repeat(501);

        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "%s",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """.formatted(longDescription));
    }

    @Test
    void createProductShouldRejectMissingPrice() throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectZeroPrice() throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "price": 0,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectNegativePrice() throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "price": -5,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectMissingCategoryId() throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "price": 19.99,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectNonPositiveCategoryId()
            throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "price": 19.99,
                  "categoryId": 0,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createProductShouldRejectNegativeStockQuantity()
            throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": -1
                }
                """);
    }

    @Test
    void createProductShouldRejectMissingStockQuantity()
            throws Exception {
        assertInvalidRequest("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "A toy",
                  "price": 19.99,
                  "categoryId": 1
                }
                """);
    }

    @Test
    void createProductShouldAcceptZeroStockQuantity()
            throws Exception {
        Product product = new Product(   UUID.randomUUID(),
                "Cosmic Cat Toy",
                "A toy for cosmic cats",
                new BigDecimal("19.99"),
                1L,
                10);

        when(productMapper.toDomain(any(ProductRequest.class)))
                .thenReturn(product);

        when(productService.create(any(Product.class)))
                .thenReturn(product);

        when(productService.create(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cosmic Cat Toy",
                                  "description": "A toy",
                                  "price": 19.99,
                                  "categoryId": 1,
                                  "stockQuantity": 0
                                }
                                """))
                .andExpect(status().is2xxSuccessful());

        verify(productService).create(any(Product.class));
    }

    private void assertInvalidRequest(String requestBody)
            throws Exception {

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
}
