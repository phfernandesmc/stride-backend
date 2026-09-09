package com.stride.api.auth;

import com.stride.api.auth.TokenService.TokenResponse;
import com.stride.api.user.EmailAlreadyUsedException;
import com.stride.api.user.RegisterRequest;
import com.stride.api.user.UserResponse;
import com.stride.api.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final UserService userService;

  public AuthController(
      AuthenticationManager authenticationManager,
      TokenService tokenService,
      UserService userService) {
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
    this.userService = userService;
  }

  /**
   * Cria a conta. Devolve 201 com os dados publicos do usuario, sem token: o cadastro nao autentica
   * ninguem, quem autentica e o /login logo em seguida.
   */
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest request) {
    return UserResponse.from(
        userService.register(request.name(), request.email(), request.password()));
  }

  /**
   * Troca e-mail e senha por um token. Quem confere a senha é o AuthenticationManager: ele chama o
   * StrideUserDetailsService para buscar o usuário e o Argon2PasswordEncoder para comparar o hash.
   * Nenhuma dessas peças aparece aqui, e é essa a ideia.
   */
  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    var authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.email(), request.password()));

    return tokenService.issue((StrideUser) authentication.getPrincipal());
  }

  /**
   * Senha errada e usuário inexistente devolvem exatamente a mesma resposta, de propósito:
   * mensagens diferentes deixariam descobrir quais e-mails existem cadastrados.
   */
  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public void handleBadCredentials() {}

  /** E-mail ja cadastrado: 409 Conflict. */
  @ExceptionHandler(EmailAlreadyUsedException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public void handleEmailAlreadyUsed() {}
}
