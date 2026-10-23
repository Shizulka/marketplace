package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import com.cosmocats.marketplace.application.service.ProductService;
import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.presentation.controller.ProductController;
import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import com.cosmocats.marketplace.presentation.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(ProductMapper.class)
class ProductControllerCrudTest {

    private static final String VALIDATION_TYPE =
            "https://cosmo-cats.market/errors/validation";
    private static final String NOT_FOUND_TYPE =
            "https://cosmo-cats.market/errors/product-not-found";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private static Product product(UUID id, String name) {
        return new Product(id, name, "Cosmic description",
                new BigDecimal("19.99"), 1L, 10);
    }

    private static String json(String name, String price, String categoryId,
                               String stock) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"description\": \"A toy\"");
        if (name != null) {
            sb.append(", \"name\": \"").append(name).append("\"");
        }
        if (price != null) {
            sb.append(", \"price\": ").append(price);
        }
        if (categoryId != null) {
            sb.append(", \"categoryId\": ").append(categoryId);
        }
        if (stock != null) {
            sb.append(", \"stockQuantity\": ").append(stock);
        }
        return sb.append("}").toString();
    }

    private static String validJson() {
        return json("Cosmic Cat Toy", "19.99", "1", "10");
    }


    @Test
    void findAllShouldReturnPagedEnvelope() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(productService.findAll(0, 10)).thenReturn(new PagedResponse<>(
                List.of(product(first, "Galaxy Yarn"), product(second, "Comet Toy")),
                0, 10, 2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(first.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Galaxy Yarn"))
                .andExpect(jsonPath("$.content[0].price").value(19.99))
                .andExpect(jsonPath("$.content[1].name").value("Comet Toy"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void findAllShouldUseDefaultPageZeroAndSizeTen() throws Exception {
        when(productService.findAll(0, 10))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        verify(productService).findAll(0, 10);
    }

    @Test
    void findAllShouldPassPageAndSizeToService() throws Exception {
        when(productService.findAll(2, 5)).thenReturn(new PagedResponse<>(
                List.of(product(UUID.randomUUID(), "Star Map")), 2, 5, 11));

        mockMvc.perform(get("/api/products")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(productService).findAll(2, 5);
    }

    @Test
    void findAllShouldReturnEmptyContentForPageOutOfRange() throws Exception {
        when(productService.findAll(99, 10))
                .thenReturn(new PagedResponse<>(List.of(), 99, 10, 3));

        mockMvc.perform(get("/api/products").param("page", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "page=-1",          // negative page
            "size=0",           // size must be >= 1
            "size=-5",          // negative size
            "page=abc",         // not a number
            "size=ten",         // not a number
            "page=1.5"          // not an integer
    })
    void findAllShouldRejectInvalidPaginationParameters(String query)
            throws Exception {
        mockMvc.perform(get("/api/products?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }


    @Test
    void findByIdShouldReturnProduct() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenReturn(product(id, "Galaxy Yarn"));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Galaxy Yarn"))
                .andExpect(jsonPath("$.description").value("Cosmic description"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void findByIdShouldReturnRfc9457ProblemWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenThrow(new ProductNotFoundException(id));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(NOT_FOUND_TYPE))
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(
                        "Product with id " + id + " was not found."))
                .andExpect(jsonPath("$.instance").value("/api/products/" + id));
    }

    @Test
    void findByIdShouldRejectMalformedUuid() throws Exception {
        mockMvc.perform(get("/api/products/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }


    @Test
    void createShouldReturn201WithLocationAndBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.create(any(Product.class)))
                .thenReturn(product(id, "Cosmic Cat Toy"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Cosmic Cat Toy"));
    }

    @Test
    void createShouldMapRequestFieldsToDomainProduct() throws Exception {
        when(productService.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Star Collar", "7.50", "3", "0")))
                .andExpect(status().isCreated());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productService).create(captor.capture());
        Product sent = captor.getValue();
        assertEquals("Star Collar", sent.getName());
        assertEquals(0, new BigDecimal("7.50").compareTo(sent.getPrice()));
        assertEquals(3L, sent.getCategoryId());
        assertEquals(0, sent.getStockQuantity());
    }

    @Test
    void createShouldReturnRfc9457ProblemForInvalidPrice() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cosmic Cat Toy", "0", "1", "10")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(VALIDATION_TYPE))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(
                        containsString("Field 'price': Price must be greater than 0")))
                .andExpect(jsonPath("$.instance").value("/api/products"));

        verifyNoInteractions(productService);
    }

    @Test
    void createShouldReportEveryInvalidFieldInDetail() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("AB", "-1", "0", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(VALIDATION_TYPE))
                .andExpect(jsonPath("$.detail").value(containsString("Field 'name'")))
                .andExpect(jsonPath("$.detail").value(containsString("Field 'price'")))
                .andExpect(jsonPath("$.detail").value(containsString("Field 'categoryId'")))
                .andExpect(jsonPath("$.detail").value(
                        containsString("Field 'stockQuantity'")));

        verifyNoInteractions(productService);
    }

    @Test
    void createShouldReportMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Field 'name'")))
                .andExpect(jsonPath("$.detail").value(containsString("Field 'price'")))
                .andExpect(jsonPath("$.detail").value(containsString("Field 'categoryId'")))
                .andExpect(jsonPath("$.detail").value(
                        containsString("Field 'stockQuantity'")));

        verifyNoInteractions(productService);
    }

    @Test
    void createShouldReturnProblemForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/products"));

        verifyNoInteractions(productService);
    }

    @Test
    void createShouldReturnProblemForWrongFieldType() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cosmic Cat Toy", "\"expensive\"", "1", "10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));

        verifyNoInteractions(productService);
    }

    @Test
    void createShouldReturnProblemForMissingBody() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));

        verifyNoInteractions(productService);
    }

    @Test
    void createShouldRejectUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(validJson()))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(productService);
    }


    @Test
    void updateShouldReturnUpdatedProduct() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any(Product.class)))
                .thenReturn(product(id, "Updated Comet"));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Updated Comet", "25.00", "2", "4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Updated Comet"));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productService).update(eq(id), captor.capture());
        assertEquals("Updated Comet", captor.getValue().getName());
        assertEquals(0, new BigDecimal("25.00").compareTo(captor.getValue().getPrice()));
        assertEquals(2L, captor.getValue().getCategoryId());
        assertEquals(4, captor.getValue().getStockQuantity());
    }

    @Test
    void updateShouldReturnRfc9457ProblemWhenProductDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any(Product.class)))
                .thenThrow(new ProductNotFoundException(id));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(NOT_FOUND_TYPE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").value("/api/products/" + id));
    }

    @Test
    void updateShouldReturnRfc9457ProblemForInvalidBody() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cosmic Cat Toy", "19.99", "1", "-3")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(VALIDATION_TYPE))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(containsString(
                        "Field 'stockQuantity': Stock quantity must not be negative")))
                .andExpect(jsonPath("$.instance").value("/api/products/" + id));

        verifyNoInteractions(productService);
    }

    @Test
    void updateShouldRejectMalformedUuid() throws Exception {
        mockMvc.perform(put("/api/products/{id}", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void updateShouldReturnProblemForMalformedJson() throws Exception {
        mockMvc.perform(put("/api/products/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));

        verifyNoInteractions(productService);
    }


    @Test
    void deleteShouldReturn204WithEmptyBody() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService).delete(id);
    }

    @Test
    void deleteShouldReturnRfc9457ProblemWhenProductDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProductNotFoundException(id)).when(productService).delete(id);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(NOT_FOUND_TYPE))
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.instance").value("/api/products/" + id));
    }

    @Test
    void deleteShouldRejectMalformedUuid() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", "123"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }


    private void assertCreateAccepted(String body) throws Exception {
        when(productService.create(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(productService).create(any(Product.class));
    }

    @Test
    void createShouldAcceptNameWithMinimumLength() throws Exception {
        assertCreateAccepted(json("abc", "19.99", "1", "10"));
    }

    @Test
    void createShouldAcceptNameWithMaximumLength() throws Exception {
        assertCreateAccepted(json("a".repeat(100), "19.99", "1", "10"));
    }

    @Test
    void createShouldAcceptMinimumPrice() throws Exception {
        assertCreateAccepted(json("Cosmic Cat Toy", "0.01", "1", "10"));
    }

    @Test
    void createShouldAcceptMissingDescription() throws Exception {
        assertCreateAccepted("""
                {
                  "name": "Cosmic Cat Toy",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """);
    }

    @Test
    void createShouldAcceptDescriptionWithMaximumLength() throws Exception {
        assertCreateAccepted("""
                {
                  "name": "Cosmic Cat Toy",
                  "description": "%s",
                  "price": 19.99,
                  "categoryId": 1,
                  "stockQuantity": 10
                }
                """.formatted("d".repeat(500)));
    }

    @Test
    void updateShouldAcceptMissingDescription() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any(Product.class)))
                .thenReturn(product(id, "Updated Comet"));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Comet",
                                  "price": 25.00,
                                  "categoryId": 2,
                                  "stockQuantity": 0
                                }
                                """))
                .andExpect(status().isOk());

        verify(productService).update(eq(id), any(Product.class));
    }

    @Test
    void findAllShouldAcceptMinimumPageSizeOfOne() throws Exception {
        when(productService.findAll(0, 1)).thenReturn(new PagedResponse<>(
                List.of(product(UUID.randomUUID(), "Star Map")), 0, 1, 3));

        mockMvc.perform(get("/api/products").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(productService).findAll(0, 1);
    }

    @Test
    void findAllShouldReturnEmptyEnvelopeWhenThereAreNoProducts() throws Exception {
        when(productService.findAll(0, 10))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }


    @Test
    void patchShouldNotBeSupported() throws Exception {
        mockMvc.perform(patch("/api/products/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(productService);
    }
}