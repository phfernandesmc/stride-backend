package com.stride.api.auth;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * O usuário como o Spring Security enxerga. Existe além da entidade de domínio porque precisamos
 * carregar o id (UUID) junto: ele vira a claim "sub" do token, e é por ele que os endpoints vão
 * filtrar os dados do dono.
 */
public record StrideUser(UUID id, String email, String passwordHash, List<String> roles)
    implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
  }

  /** O hash Argon2 vindo do banco. O DaoAuthenticationProvider compara a senha digitada com ele. */
  @Override
  public String getPassword() {
    return passwordHash;
  }

  /** Para o Spring, "username" é o identificador do login. No nosso caso, o e-mail. */
  @Override
  public String getUsername() {
    return email;
  }
}
