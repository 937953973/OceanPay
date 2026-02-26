package com.oceanpay.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:oceanpay-secret-key-2025-spring-boot-jwt-security}")
    private String secret;

    @Value("${jwt.issuer:OceanPay}")
    private String issuer;

    /**
     * 生成JWT令牌
     */
    public String generateToken(Map<String, Object> claims, long expirationSeconds) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationSeconds * 1000);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     */
    public Map<String, Object> parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // JJWT 0.12.x 使用 Jwts.parser() 而不是 Jwts.parserBuilder()
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new HashMap<>(claims);
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT令牌: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.warn("JWT令牌格式错误: {}", e.getMessage());
            return null;
        } catch (SecurityException e) {
            log.warn("JWT令牌签名验证失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("JWT令牌参数错误: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("解析JWT令牌时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证JWT令牌是否有效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从令牌中获取指定声明
     */
    public Object getClaim(String token, String claimName) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? claims.get(claimName) : null;
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserId(String token) {
        Object userId = getClaim(token, "userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsername(String token) {
        Object username = getClaim(token, "username");
        return username != null ? username.toString() : null;
    }

    /**
     * 从令牌中获取邮箱
     */
    public String getEmail(String token) {
        Object email = getClaim(token, "email");
        return email != null ? email.toString() : null;
    }

    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("检查令牌过期状态时发生错误: {}", e.getMessage(), e);
            return true;
        }
    }

    /**
     * 获取令牌过期时间
     */
    public Date getExpirationDate(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (Exception e) {
            log.error("获取令牌过期时间时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取令牌剩余有效时间（秒）
     */
    public Long getRemainingTime(String token) {
        Date expiration = getExpirationDate(token);
        if (expiration == null) {
            return null;
        }

        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        return remainingMillis > 0 ? remainingMillis / 1000 : 0;
    }
}