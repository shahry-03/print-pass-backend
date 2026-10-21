package com.printpass.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Yaml file me se secret key uthaenge
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Yaml file me se token ki expiry time uthaenge
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;   // User Service me nahi tha yaha he

    // Secret Key ko read karne ka helper method
    private Key getSignInKey() {
        // ⚠️ Zaroori Note: Yahan wahi Decoder use karein jo aapne User Service mein use kiya hai!
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Asli Functionality: JWT Token generate karne ka method
    public String generateToken(String email, String userId, String role) {
        // Yahan par aap JWT token generate karne ka logic likh sakte hain
        // Aap JJWT library ka use kar sakte hain, jisme aap claims set kar sakte hain,
        // expiration time set kar sakte hain, aur secret key se sign kar sakte hain.


        // Token ke andar extra information (Claims) dalne ke liye
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId); // userId claim me dalo
        extraClaims.put("role", role) ;     // role claim me dalo

        return Jwts.builder()
                .setClaims(extraClaims)  // Extra claims (userId, role) dal di
                .setSubject(email)       // Asli identity (Email) set kardi
                .setIssuedAt(new Date()) // Token kab banaya gaya
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))  // Token expire kab hoga
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // Secret key aur algorithm se sign kar diya
                .compact(); // Token ko string me convert kar diya
    }

    // Expiration time return karo (LoginResponse ke liye)
    public long getExpirationTime(){
        return jwtExpiration;
    }


}
