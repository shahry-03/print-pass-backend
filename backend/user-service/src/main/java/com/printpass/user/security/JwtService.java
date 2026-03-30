package com.printpass.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    // Yeh hamara bohot hi secret password hai jisse token lock hoga (Kam az kam 256-bit lamba hona chahiye)
    // Asal production mein isey application.yml mein rakhte hain, abhi aasan rakhne ke liye yahan likh rahe hain
    // Yeh application.yaml se aayegi taake code mein hardcode na ho
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;


    // Secret key ko readable format mein convert karo
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Token se saare claims nikalo (private)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())   // Same secret key se verify
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



    // 1. Token se Email nikalo
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject(); // Subject mein email hoga
    }

    // 2. Token se Role nikalo
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class); // Claims mein role hoga
    }

    // 4. Token se User Id nikalo
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class); // Claims mein userId hoga
    }

    // 4. Token expire hua ya nahi ye method Private hona chahiye
    // — Sirf isTokenValid() use karega
    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration(); // Claims mein expiration hoga
        return expiration.before(new Date()); // Agar expiration date aaj se pehle hai, toh token expire ho chuka hai
    }

    // 5. Token Valid he ya nahi
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);  // expire nahi hua to valid
        } catch (Exception e){
            log.error("Invalid Jwt Token: {}", e.getMessage());
            return false;
        }
    }
}