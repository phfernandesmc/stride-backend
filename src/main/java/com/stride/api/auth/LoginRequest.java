package com.stride.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo do POST /api/auth/login. A validação roda antes de qualquer consulta ao banco. */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
