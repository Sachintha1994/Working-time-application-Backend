package com.thilina.WorkingTimeApplication.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility class for token generation and validation
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:myVerySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days for refresh token
    private Long refreshExpiration;

    /**
     * Get signing key from secret
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate JWT access token for user
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "Bearer");
        claims.put("tokenType", "ACCESS");

        log.debug("Generating access token for user: {}", username);
        return createToken(claims, username, expiration);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "Bearer");
        claims.put("tokenType", "REFRESH");

        log.debug("Generating refresh token for user: {}", username);
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * Create token with claims and custom expiration
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .setId(java.util.UUID.randomUUID().toString()) // Add unique token ID
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extract token type (ACCESS or REFRESH)
     */
    public String extractTokenType(String token) {
        Object tokenType = extractAllClaims(token).get("tokenType");
        return tokenType != null ? tokenType.toString() : "ACCESS";
    }

    /**
     * Extract token ID
     */
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract issued at date from token
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired: {}", e.getMessage());
            throw new RuntimeException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw new RuntimeException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT token is invalid: {}", e.getMessage());
            throw new RuntimeException("JWT token is invalid", e);
        }
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validate token (basic validation without username check)
     */
    public Boolean validateToken(String token) {
        try {
            extractUsername(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token against username
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(username) && !isTokenExpired(token);

            if (isValid) {
                log.debug("Token validation successful for user: {}", username);
            } else {
                log.warn("Token validation failed for user: {}", username);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Token validation error for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if token can be refreshed (not expired beyond refresh window)
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            Date expirationDate = extractExpiration(token);
            Date now = new Date();

            // Token can be refreshed if it expired within the last hour
            long hourInMillis = 3600000L;
            return now.getTime() - expirationDate.getTime() < hourInMillis;
        } catch (Exception e) {
            log.error("Error checking if token can be refreshed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get remaining validity time in milliseconds
     */
    public Long getRemainingValidity(String token) {
        try {
            Date expirationDate = extractExpiration(token);
            Date now = new Date();
            return Math.max(0, expirationDate.getTime() - now.getTime());
        } catch (Exception e) {
            log.error("Error getting remaining validity: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Check if token is about to expire (within 5 minutes)
     */
    public Boolean isTokenAboutToExpire(String token) {
        try {
            Long remainingTime = getRemainingValidity(token);
            long fiveMinutesInMillis = 300000L;
            return remainingTime < fiveMinutesInMillis && remainingTime > 0;
        } catch (Exception e) {
            log.error("Error checking if token is about to expire: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get token expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return expiration;
    }

    /**
     * Get refresh token expiration time in milliseconds
     */
    public Long getRefreshExpirationTime() {
        return refreshExpiration;
    }

    /**
     * Extract all information from token
     */
    public Map<String, Object> getTokenInfo(String token) {
        Map<String, Object> info = new HashMap<>();
        try {
            info.put("username", extractUsername(token));
            info.put("role", extractRole(token));
            info.put("tokenType", extractTokenType(token));
            info.put("tokenId", extractTokenId(token));
            info.put("issuedAt", extractIssuedAt(token));
            info.put("expiresAt", extractExpiration(token));
            info.put("isExpired", isTokenExpired(token));
            info.put("remainingValidity", getRemainingValidity(token));
        } catch (Exception e) {
            log.error("Error extracting token info: {}", e.getMessage());
        }
        return info;
    }
}