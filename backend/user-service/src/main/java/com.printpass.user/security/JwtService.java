package main.java.com.printpass.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Yeh hamara bohot hi secret password hai jisse token lock hoga (Kam az kam 256-bit lamba hona chahiye)
    // Asal production mein isey application.yml mein rakhte hain, abhi aasan rakhne ke liye yahan likh rahe hain
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    // Token banane ka main method
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Hum token ke andar user ka role (STUDENT/VENDOR) bhi chupa rahe hain
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // Kiske naam par token ban raha hai?
                .setIssuedAt(new Date(System.currentTimeMillis())) // Kab bana?
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 din (24 hours) baad expire ho jayega
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // Secret key se stamp (mohar) lagayen
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}