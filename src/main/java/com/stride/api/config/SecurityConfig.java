package com.stride.api.config;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

  /** Rotas acessíveis sem autenticação. */
  private static final String[] PUBLIC_ENDPOINTS = {
    "/api/auth/login",
    "/api/auth/register",
    "/v3/api-docs/**",
    "/swagger-ui.html",
    "/swagger-ui/**",
    // O despacho de ERRO tambem passa pelo filtro de seguranca. Sem liberar
    // /error, qualquer excecao vira 401 e esconde a causa real.
    "/error"
  };

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter)
      throws Exception {
    return http
        // API autenticada por token: não há sessão nem formulário HTML,
        // então não existe o vetor que o CSRF protege.
        // Sem isto o navegador bloqueia qualquer chamada vinda do frontend,
        // que roda em outra origem. Le a configuracao do bean abaixo.
        .cors(cors -> {})
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(PUBLIC_ENDPOINTS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
        .build();
  }

  /**
   * Diz ao navegador quais origens podem chamar a API. O preflight (a requisicao OPTIONS que o
   * navegador manda antes) e respondido por este filtro antes da autenticacao, por isso nao precisa
   * entrar na lista de rotas publicas.
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(properties.allowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    // O front precisa ler o header Authorization? Nao: ele proprio o envia.
    // Mantemos exposto so o necessario.
    config.setExposedHeaders(List.of("Location"));
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * A peça que confere a senha no login. O DaoAuthenticationProvider busca o usuário pelo
   * UserDetailsService e compara a senha digitada com o hash usando o PasswordEncoder abaixo.
   *
   * <p>Atenção: na 7.x o UserDetailsService vai no construtor. O padrão antigo, com construtor
   * vazio mais setUserDetailsService(), não existe mais.
   */
  @Bean
  AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }
}
