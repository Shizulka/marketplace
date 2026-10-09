
package com.cosmocats.marketplace.application.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setType(URI.create(
                "https://cosmo-cats.market/errors/product-not-found"
        ));
        problem.setTitle("Product not found");
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "Field '" + error.getField()
                        + "': " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                details
        );

        problem.setType(URI.create(
                "https://cosmo-cats.market/errors/validation"
        ));
        problem.setTitle("Validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }

}
