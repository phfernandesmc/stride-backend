package com.stride.api.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origens autorizadas a chamar a API pelo navegador, lidas de stride.cors.allowed-origins.
 *
 * @param allowedOrigins lista exata de origens (esquema + host + porta). Curinga "*" nao funciona
 *     junto com credenciais, por isso as origens sao declaradas uma a uma.
 */
@ConfigurationProperties(prefix = "stride.cors")
public record CorsProperties(List<String> allowedOrigins) {}
