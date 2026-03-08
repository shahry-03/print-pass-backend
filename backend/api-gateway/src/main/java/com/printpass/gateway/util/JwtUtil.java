package com.printpass.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    // Secret key application.yml se aayegi
    @Value("${jwt.secret}")
    private String secret;

    // 1. Token Check Karne Ka Naya Method (JJWT 0.12+ syntax)
    public void validateToken(final String token) {
        Jwts.parser()
                .verifyWith(getSignKey()) // 🌟 NAYA: parserBuilder() ki jagah parser().verifyWith()
                .build()
                .parseSignedClaims(token);
    }

    // 2. Token ke andar se data (Claims) nikalne ka Naya Method
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // 🌟 NAYA: getBody() ab getPayload() ban gaya hai!
    }

    // 3. Secret String ko Asli Key mein badalne ka method
    private SecretKey getSignKey() { // 🌟 NAYA: Key ki jagah SecretKey return karega
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}