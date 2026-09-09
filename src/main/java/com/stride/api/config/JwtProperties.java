package com.stride.api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Configuração do JWT, lida de stride.jwt.* no application.properties.
 *
 * @param issuer quem emite o token; vai na claim "iss" e é validado na entrada
 * @param accessTokenTtl por quanto tempo o token vale (claim "exp")
 * @param privateKey PEM PKCS#8 usado para ASSINAR (segredo)
 * @param publicKey PEM X.509 usado para VERIFICAR (pode ser distribuído)
 */
@ConfigurationProperties(prefix = "stride.jwt")
public record JwtProperties(
    String issuer, Duration accessTokenTtl, Resource privateKey, Resource publicKey) {}
