package com.app.koratime.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
/**
 * binds all app configurations in .yml to this class directly so we don't use @value("${}").
 * spring validates if any property missing the apps fails at startup.
 */
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Cloudinary cloudinary = new Cloudinary();
    private final RateLimit rateLimit = new RateLimit();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
    }

    @Data
    public static class Cloudinary {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
    }

    @Data
    public static class RateLimit {
        private int capacity;
        private int refillTokens;
        private int refillSeconds;
    }


}
