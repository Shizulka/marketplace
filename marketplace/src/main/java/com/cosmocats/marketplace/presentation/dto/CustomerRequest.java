package com.cosmocats.marketplace.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Full name must not be blank")
        @Size(max = 150, message = "Full name must not exceed 150 characters")
        String fullName) {
}
