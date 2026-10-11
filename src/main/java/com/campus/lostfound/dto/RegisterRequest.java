package com.campus.lostfound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Enter your name") @Size(max = 80) String name,
        @NotBlank(message = "Enter your email") @Email(message = "Enter a valid email") String email,
        @Size(max = 20, message = "Phone number is too long") String phone,
        @NotBlank(message = "Choose a password") @Size(min = 6, message = "Use at least 6 characters") String password) {}
