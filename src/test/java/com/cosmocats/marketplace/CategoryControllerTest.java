package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CategoryNotFoundException;
import com.cosmocats.marketplace.application.service.CategoryService;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSummary;
import com.cosmocats.marketplace.presentation.controller.CategoryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CategoryService categoryService;

    private static CategoryEntity category(long id, String name, String description) {
        CategoryEntity entity = new CategoryEntity(name, description);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    void createShouldReturn201WithLocation() throws Exception {
        when(categoryService.create("Toys", "Cosmic toys")).thenReturn(category(1L, "Toys", "Cosmic toys"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Toys\",\"description\":\"Cosmic toys\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/categories/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Toys"));
    }

    @Test
    void createShouldReturn400ForBlankName() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllShouldReturnAllOrSearchResults() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(category(1L, "Toys", null), category(2L, "Food", null)));
        when(categoryService.search("to")).thenReturn(List.of(category(1L, "Toys", null)));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/categories").param("search", "to"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Toys"));
    }

    @Test
    void findByIdShouldReturnCategoryOr404() throws Exception {
        when(categoryService.findById(1L)).thenReturn(category(1L, "Toys", "d"));
        when(categoryService.findById(99L)).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("d"));
        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findProductsShouldReturnSummaries() throws Exception {
        UUID id = UUID.randomUUID();
        ProductSummary summary = new ProductSummary() {
            @Override public UUID getId() { return id; }
            @Override public String getName() { return "Yarn"; }
            @Override public BigDecimal getPrice() { return new BigDecimal("9.99"); }
        };
        when(categoryService.findProductSummaries(1L)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/categories/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Yarn"))
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    void updateShouldReturnUpdatedCategory() throws Exception {
        when(categoryService.update(1L, "Food", "Galaxy food")).thenReturn(category(1L, "Food", "Galaxy food"));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\",\"description\":\"Galaxy food\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }
}
