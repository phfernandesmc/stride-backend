package com.stride.api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo do POST /api/auth/register. */
public record RegisterRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 180) String email,
    // O limite de 180 vem da coluna; o minimo de 8 e regra nossa.
    @NotBlank @Size(min = 8, max = 180) String password) {}
