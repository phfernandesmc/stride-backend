package com.stride.api.user;

import java.time.Instant;
import java.util.UUID;

/**
 * O usuário como o domínio enxerga. Note que carrega o hash da senha, nunca a senha em si — ela
 * existe só durante a requisição de cadastro e nunca é persistida nem logada.
 */
public record User(UUID id, String name, String email, String passwordHash, Instant createdAt) {}
