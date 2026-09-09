package com.stride.api.auth;

import com.stride.api.config.JwtProperties;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Monta e assina o access token. */
@Service
public class TokenService {

  private final JwtEncoder encoder;
  private final JwtProperties properties;

  public TokenService(JwtEncoder encoder, JwtProperties properties) {
    this.encoder = encoder;
    this.properties = properties;
  }

  public TokenResponse issue(StrideUser user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(properties.accessTokenTtl());

    // Estas claims viajam em texto legível dentro do token: a assinatura
    // impede alteração, não leitura. Nada sensível aqui.
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(user.id().toString())
            .claim("email", user.email())
            .claim("roles", user.roles())
            .build();

    String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    return new TokenResponse(token, "Bearer", properties.accessTokenTtl().toSeconds());
  }

  /** Formato de resposta do RFC 6750: o cliente manda "Authorization: Bearer <accessToken>". */
  public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
}
