package com.citizenconnect.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "mysecretkeymysecretkeymysecretkey";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * Strips a leading {@code Bearer } prefix (case-insensitive). If absent, returns the trimmed value
     * (raw JWT). Use for {@code Authorization} headers and for controller/service token arguments.
     */
    public static String stripBearerPrefix(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return "";
        }
        String t = authorizationHeader.trim();
        if (t.length() > 7 && t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return t.substring(7).trim();
        }
        return t;
    }

    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(key)
                .compact();
    }

    public static String extractEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtException("Missing token");
        }
        token = stripBearerPrefix(token);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}