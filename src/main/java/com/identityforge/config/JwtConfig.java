package com.identityforge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtConfig {
    private String secret;
    private int expirationMinutes = 60;
    private int refreshExpirationDays = 7;

    public long getExpirationMs() {
        return (long) expirationMinutes * 60 * 1000;
    }

    public long getRefreshExpirationMs() {
        return (long) refreshExpirationDays * 24 * 60 * 60 * 1000;
    }
}
