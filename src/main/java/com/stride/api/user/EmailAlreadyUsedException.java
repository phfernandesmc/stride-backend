package com.stride.api.user;

/** E-mail já cadastrado. Vira HTTP 409 na borda. */
public class EmailAlreadyUsedException extends RuntimeException {

  public EmailAlreadyUsedException(String email) {
    super("E-mail ja cadastrado: " + email);
  }
}
