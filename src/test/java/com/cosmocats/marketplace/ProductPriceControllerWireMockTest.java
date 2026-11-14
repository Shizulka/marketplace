package com.cosmocats.marketplace;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductPriceControllerWireMockTest extends AbstractPostgresTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("exchange-rate.base-url", wireMock::baseUrl);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ProductJpaRepository productJpaRepository;

    /** БД спільна для всіх тестів, тому прибираємо за собою. */
    @AfterEach
    void cleanUp() {
        productJpaRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Product saveProduct() {
        // продукт тепер зберігається в БД, тому категорія має існувати; імена унікальні між тестами
        Long categoryId = categoryRepository
                .save(new CategoryEntity("Toys-" + UUID.randomUUID(), null)).getId();
        return productRepository.save(new Product(
                null, "Cosmic Cat Toy", "A toy",
                new BigDecimal("20.00"), categoryId, 5));
    }

    @Test
    void shouldConvertPriceUsingStubbedRate() throws Exception {
        Product product = saveProduct();
        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathEqualTo("/api/rates"))
                .withQueryParam("target", equalTo("EUR"))
                .willReturn(okJson("{\"base\":\"USD\",\"target\":\"EUR\",\"rate\":0.5}")));

        mockMvc.perform(get("/api/products/{id}/price", product.getId())
                        .param("currency", "eur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.price").value(10.0));
    }

    @Test
    void shouldNotCallExternalServiceForBaseCurrency() throws Exception {
        Product product = saveProduct();

        mockMvc.perform(get("/api/products/{id}/price", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.price").value(20.0));

        wireMock.verify(0, getRequestedFor(urlPathEqualTo("/api/rates")));
    }

    @Test
    void shouldReturn400ForUnsupportedCurrency() throws Exception {
        Product product = saveProduct();
        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathEqualTo("/api/rates"))
                .withQueryParam("target", equalTo("XXX"))
                .willReturn(notFound()));

        mockMvc.perform(get("/api/products/{id}/price", product.getId())
                        .param("currency", "XXX"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn502WhenRateServiceFails() throws Exception {
        Product product = saveProduct();
        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathEqualTo("/api/rates"))
                .withQueryParam("target", equalTo("GBP"))
                .willReturn(serverError()));

        mockMvc.perform(get("/api/products/{id}/price", product.getId())
                        .param("currency", "GBP"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void shouldReturn404ForUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}/price", UUID.randomUUID())
                        .param("currency", "EUR"))
                .andExpect(status().isNotFound());
    }
}