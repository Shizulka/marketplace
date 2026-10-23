package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.GlobalExceptionHandler;
import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/products/123");
    }

    @Test
    void handleProductNotFoundShouldReturnNotFoundProblem() {
        ProductNotFoundException exception =
                mock(ProductNotFoundException.class);

        when(exception.getMessage()).thenReturn("Product not found");

        ProblemDetail result =
                handler.handleProductNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("Product not found", result.getTitle());
        assertEquals("Product not found", result.getDetail());
        assertEquals(
                URI.create(
                        "https://cosmo-cats.market/errors/product-not-found"
                ),
                result.getType()
        );
        assertEquals(
                URI.create("/api/products/123"),
                result.getInstance()
        );
    }

    @Test
    void handleValidationShouldReturnBadRequestProblem() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "productRequest",
                "name",
                "Product name must not be blank"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ProblemDetail result =
                handler.handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Validation failed", result.getTitle());
        assertEquals(
                "Field 'name': Product name must not be blank",
                result.getDetail()
        );
        assertEquals(
                URI.create("https://cosmo-cats.market/errors/validation"),
                result.getType()
        );
        assertEquals(
                URI.create("/api/products/123"),
                result.getInstance()
        );
    }

    @Test
    void handleValidationShouldCombineMultipleFieldErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError nameError = new FieldError(
                "productRequest",
                "name",
                "Product name must not be blank"
        );

        FieldError priceError = new FieldError(
                "productRequest",
                "price",
                "Price must be greater than 0"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(nameError, priceError));

        ProblemDetail result =
                handler.handleValidation(exception, request);

        assertEquals(
                "Field 'name': Product name must not be blank; "
                        + "Field 'price': Price must be greater than 0",
                result.getDetail()
        );
    }

    @Test
    void handleValidationShouldReturnEmptyDetailWhenNoFieldErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ProblemDetail result =
                handler.handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Validation failed", result.getTitle());
        assertEquals("", result.getDetail());
    }

    @Test
    void handleUnreadableRequestShouldReturnBadRequestProblem() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ProblemDetail result =
                handler.handleUnreadableRequest(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Invalid request body", result.getTitle());
        assertEquals(
                "Request body is missing or contains invalid JSON.",
                result.getDetail()
        );
        assertEquals(
                URI.create("/api/products/123"),
                result.getInstance()
        );
    }

    @Test
    void handleUnreadableRequestShouldNotSetType() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ProblemDetail result =
                handler.handleUnreadableRequest(exception, request);

        assertNull(result.getType());
    }

    @Test
    void handleConstraintViolationShouldReturnBadRequestProblem() {
        ConstraintViolationException exception =
                mock(ConstraintViolationException.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation =
                mock(ConstraintViolation.class);

        when(violation.getMessage())
                .thenReturn("Category ID must be positive");

        when(exception.getConstraintViolations())
                .thenReturn(Set.of(violation));

        ProblemDetail result =
                handler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Invalid request parameters", result.getTitle());
        assertEquals(
                "Category ID must be positive",
                result.getDetail()
        );
        assertEquals(
                URI.create("https://cosmo-cats.market/errors/validation"),
                result.getType()
        );
        assertEquals(
                URI.create("/api/products/123"),
                result.getInstance()
        );
    }

    @Test
    void handleConstraintViolationShouldCombineMultipleViolations() {
        ConstraintViolationException exception =
                mock(ConstraintViolationException.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> firstViolation =
                mock(ConstraintViolation.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> secondViolation =
                mock(ConstraintViolation.class);

        when(firstViolation.getMessage())
                .thenReturn("Category ID must be positive");

        when(secondViolation.getMessage())
                .thenReturn("Price must be greater than 0");

        when(exception.getConstraintViolations())
                .thenReturn(Set.of(firstViolation, secondViolation));

        ProblemDetail result =
                handler.handleConstraintViolation(exception, request);

        assertTrue(result.getDetail().contains(
                "Category ID must be positive"
        ));
        assertTrue(result.getDetail().contains(
                "Price must be greater than 0"
        ));
        assertTrue(result.getDetail().contains("; "));
    }

    @Test
    void handleConstraintViolationShouldReturnEmptyDetailWhenNoViolations() {
        ConstraintViolationException exception =
                mock(ConstraintViolationException.class);

        when(exception.getConstraintViolations()).thenReturn(Set.of());

        ProblemDetail result =
                handler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Invalid request parameters", result.getTitle());
        assertEquals("", result.getDetail());
    }
}
