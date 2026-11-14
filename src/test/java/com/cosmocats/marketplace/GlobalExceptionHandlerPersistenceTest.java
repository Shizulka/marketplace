package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerPersistenceTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/orders/1");
    }

    @Test
    void entityNotFoundShouldReturn404ForEachNotFoundException() {
        for (RuntimeException exception : new RuntimeException[]{
                new CategoryNotFoundException(1L),
                new OrderNotFoundException(UUID.randomUUID()),
                new OrderNotFoundException("CC-1"),
                new CustomerNotFoundException(UUID.randomUUID())}) {
            ProblemDetail result = handler.handleEntityNotFound(exception, request);

            assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
            assertEquals("Resource not found", result.getTitle());
            assertEquals(exception.getMessage(), result.getDetail());
            assertEquals(URI.create("/api/orders/1"), result.getInstance());
        }
    }

    @Test
    void insufficientStockShouldReturn409() {
        InsufficientStockException exception = new InsufficientStockException(UUID.randomUUID(), 1, 5);

        ProblemDetail result = handler.handleInsufficientStock(exception, request);

        assertEquals(HttpStatus.CONFLICT.value(), result.getStatus());
        assertEquals("Insufficient stock", result.getTitle());
        assertTrue(result.getDetail().contains("available 1, requested 5"));
    }

    @Test
    void dataIntegrityViolationShouldReturn409WithoutLeakingSql() {
        ProblemDetail result = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("constraint uq_x violated"), request);

        assertEquals(HttpStatus.CONFLICT.value(), result.getStatus());
        assertEquals("Data integrity violation", result.getTitle());
        assertFalse(result.getDetail().contains("uq_x"));
    }

    @Test
    void arithmeticOverflowShouldReturn400WithoutLeakingDetails() {
        ProblemDetail result = handler.handleArithmetic(new ArithmeticException("integer overflow"), request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Invalid request", result.getTitle());
        assertEquals("Numeric value is out of the allowed range.", result.getDetail());
        assertEquals(URI.create("/api/orders/1"), result.getInstance());
    }
}