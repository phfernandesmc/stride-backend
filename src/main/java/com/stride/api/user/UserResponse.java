package com.stride.api.user;

import java.time.Instant;
import java.util.UUID;

/**
 * O que a API devolve sobre um usuário. Existe separado do record User justamente para que o
 * passwordHash não tenha como vazar numa resposta por descuido.
 */
public record UserResponse(UUID id, String name, String email, Instant createdAt) {

  public static UserResponse from(User user) {
    return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
  }
}
