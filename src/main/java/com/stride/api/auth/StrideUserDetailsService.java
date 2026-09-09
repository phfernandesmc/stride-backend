package com.stride.api.auth;

import com.stride.api.user.User;
import com.stride.api.user.UserService;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapta o usuário do domínio para o formato que o Spring Security entende. Não faz SQL: quem
 * conversa com o banco é o UserRepository, por trás do UserService.
 */
@Service
public class StrideUserDetailsService implements UserDetailsService {

  private final UserService userService;

  public StrideUserDetailsService(UserService userService) {
    this.userService = userService;
  }

  @Override
  public StrideUser loadUserByUsername(String email) {
    User user =
        userService
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

    // Ainda nao ha tabela de papeis: todo mundo e USER por enquanto.
    return new StrideUser(user.id(), user.email(), user.passwordHash(), List.of("USER"));
  }
}
