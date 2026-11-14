package com.cosmocats.marketplace.presentation.controller;

import com.cosmocats.marketplace.application.service.PriceConversionService;
import com.cosmocats.marketplace.presentation.dto.PriceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductPriceController {

    private final PriceConversionService priceConversionService;

    public ProductPriceController(PriceConversionService priceConversionService) {
        this.priceConversionService = priceConversionService;
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<PriceResponse> getPrice(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "USD") String currency) {
        return ResponseEntity.ok(new PriceResponse(
                id,
                currency.toUpperCase(),
                priceConversionService.convertPrice(id, currency)));
    }
}