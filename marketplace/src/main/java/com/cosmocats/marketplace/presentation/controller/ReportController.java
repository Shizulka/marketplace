package com.cosmocats.marketplace.presentation.controller;

import com.cosmocats.marketplace.application.service.SalesReportService;
import com.cosmocats.marketplace.presentation.dto.ProductSalesResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final SalesReportService salesReportService;

    public ReportController(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductSalesResponse>> topProducts(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        List<ProductSalesResponse> body = salesReportService.topProducts(limit, since)
                .stream()
                .map(row -> new ProductSalesResponse(
                        row.getProductId(), row.getProductName(), row.getTotalQuantity()))
                .toList();
        return ResponseEntity.ok(body);
    }
}
