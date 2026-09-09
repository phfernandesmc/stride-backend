package com.stride.api.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/** Monta as duas metades do JWT: quem assina (encoder) e quem confere (decoder). */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

  /** Lê o PEM X.509 (-----BEGIN PUBLIC KEY-----) e vira um objeto de chave. */
  @Bean
  RSAPublicKey rsaPublicKey(JwtProperties properties) throws IOException {
    try (InputStream in = properties.publicKey().getInputStream()) {
      return RsaKeyConverters.x509().convert(in);
    }
  }

  /** Lê o PEM PKCS#8 (-----BEGIN PRIVATE KEY-----). Este é o segredo do sistema. */
  @Bean
  RSAPrivateKey rsaPrivateKey(JwtProperties properties) throws IOException {
    try (InputStream in = properties.privateKey().getInputStream()) {
      return RsaKeyConverters.pkcs8().convert(in);
    }
  }

  /**
   * Assina tokens. O par de chaves é embrulhado num JWK (o formato JSON padrão para chaves) porque
   * é disso que o Nimbus, a biblioteca por baixo, precisa. O keyID identifica a chave: quando você
   * tiver rotação, é ele que diz ao verificador qual chave usar.
   */
  @Bean
  JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    RSAKey jwk =
        new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
  }

  /**
   * Confere tokens que chegam. Só precisa da chave pública. O validador padrão já checa "exp" e
   * "nbf"; createDefaultWithIssuer acrescenta a checagem de "iss", para que um token assinado por
   * outro sistema não seja aceito aqui.
   */
  @Bean
  JwtDecoder jwtDecoder(RSAPublicKey publicKey, JwtProperties properties) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
    return decoder;
  }

  /**
   * Traduz claims do token em permissões do Spring Security. Por padrão ele leria a claim "scope";
   * aqui apontamos para "roles" e pedimos o prefixo ROLE_, que é o que hasRole() espera.
   */
  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("roles");
    authorities.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    return converter;
  }
}
