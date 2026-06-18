package com.app.koratime.common.security;

import com.app.koratime.common.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final AppProperties appProperties;

    public String generateAccessToken(final UserPrincipal principal) {
        return buildToken(principal, "access", appProperties.getJwt().getAccessTokenExpirationMs());
    }

    public String generateRefreshToken(final UserPrincipal principal) {
        return buildToken(principal, "refresh", appProperties.getJwt().getRefreshTokenExpirationMs());
    }

    private String buildToken(UserPrincipal principal, String tokenType, long tokenExpirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", principal.getId());
        claims.put("role", principal.getRole());
        claims.put("type", tokenType);

        return Jwts.builder()
                .claims(claims)
                .subject(principal.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public boolean isTokenValid(final String token) {
        try {
            final Claims claims = extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT empty or null: {}", e.getMessage());
        }
        return false;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public UUID extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("id", UUID.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isAccessToken(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class)).equals("access");
    }
    public boolean isRefreshToken(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class)).equals("refresh");
    }

}
