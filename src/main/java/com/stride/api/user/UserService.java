package com.stride.api.user;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de negócio de usuário. O controller não sabe nada de hash nem de duplicidade. */
@Service
public class UserService {

  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * A senha em texto puro morre aqui: entra, vira hash Argon2 e o texto original nunca sai deste
   * método.
   *
   * <p>A checagem prévia de e-mail existe para dar um erro claro, mas ela sozinha tem uma corrida:
   * dois cadastros simultâneos passariam os dois. Quem realmente garante é a constraint UNIQUE do
   * banco, e por isso o DuplicateKeyException também é tratado.
   */
  @Transactional
  public User register(String name, String email, String rawPassword) {
    if (repository.existsByEmail(email)) {
      throw new EmailAlreadyUsedException(email);
    }
    try {
      return repository.insert(name, email, passwordEncoder.encode(rawPassword));
    } catch (DuplicateKeyException e) {
      throw new EmailAlreadyUsedException(email);
    }
  }

  public java.util.Optional<User> findByEmail(String email) {
    return repository.findByEmail(email);
  }
}
