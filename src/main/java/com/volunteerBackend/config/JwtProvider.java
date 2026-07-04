package com.volunteerBackend.config;

import javax.crypto.SecretKey;
import com.volunteerBackend.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtProvider {

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken( User user) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(3600)); // 1h

        return Jwts.builder()
                .issuer("volunteer")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", user.getEmail())
                .claim("id", user.getId())
                .claim("type", "access")
                .claim("role", user.getRole())
                .signWith(getSigningKey())
                .compact();
    }

    public static String refreshAccessToken(User user) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(3600)); // 1h

        return Jwts.builder()
                .issuer("volunteer")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", user.getEmail())
                .claim("id", user.getId())
                .claim("type", "access")
                .claim("role", user.getRole())
                .signWith(getSigningKey())
                .compact();
    }

    public static String generateRefreshToken( User user) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusSeconds(365L * 24 * 3600)); // 1 năm

        return Jwts.builder()
                .issuer("volunteer")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", user.getEmail())
                .claim("id", user.getId())
                .claim("type", "refresh")
                .claim("role", user.getRole())
                .signWith(getSigningKey())
                .compact();
    }

    public static String getEmailFromJwtToken(String jwt) {
        // Remove Bearer prefix if exists
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return String.valueOf(claims.get("email"));
    }

    public static Integer getIdFromJwtToken(String jwt) {
        // Remove Bearer prefix if exists
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return (Integer) claims.get("id");
    }

    public static String getRoleFromJwtToken(String jwt) {
        // Remove Bearer prefix if exists
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        return String.valueOf(claims.get("role"));
    }

    public static boolean validateToken(String token) { // ✅ Thêm static
        try {
            // Remove Bearer prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            System.out.println("Validating token: " + token);
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateRefreshToken(String token) { // ✅ Thêm static
        try {
            // Remove Bearer prefix if exists
            // if (token.startsWith("Bearer ")) {
            //     token = token.substring(7);
            // }
            System.out.println("Validating refresh token: " + token);
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("refresh Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }
}