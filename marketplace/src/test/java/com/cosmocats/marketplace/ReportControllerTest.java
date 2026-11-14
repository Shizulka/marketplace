package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.service.SalesReportService;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSalesProjection;
import com.cosmocats.marketplace.presentation.controller.ReportController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private SalesReportService salesReportService;

    private static ProductSalesProjection row(UUID id, String name, long qty) {
        return new ProductSalesProjection() {
            @Override public UUID getProductId() { return id; }
            @Override public String getProductName() { return name; }
            @Override public Long getTotalQuantity() { return qty; }
        };
    }

    @Test
    void topProductsShouldReturnReportRows() throws Exception {
        UUID id = UUID.randomUUID();
        when(salesReportService.topProducts(eq(10), any())).thenReturn(List.of(row(id, "Yarn", 6)));

        mockMvc.perform(get("/api/reports/top-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(id.toString()))
                .andExpect(jsonPath("$[0].productName").value("Yarn"))
                .andExpect(jsonPath("$[0].totalQuantity").value(6));
    }

    @Test
    void topProductsShouldPassLimitAndSince() throws Exception {
        when(salesReportService.topProducts(5, Instant.parse("2026-01-01T00:00:00Z")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reports/top-products")
                        .param("limit", "5")
                        .param("since", "2026-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(salesReportService).topProducts(5, Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void topProductsShouldRejectInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/reports/top-products").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }
}
