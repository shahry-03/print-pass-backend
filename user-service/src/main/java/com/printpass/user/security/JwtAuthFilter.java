package com.printpass.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
             @NonNull HttpServletRequest request,
             @NonNull HttpServletResponse response,
             @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ==========================================
        // BOX 1: Header Check
        // ==========================================

        // 1. Request se "Authorization" header nikalo
        final String authHeader = request.getHeader("Authorization");
        // 2. Agar header nahi he (null) hai ya " Bearer " se start nahi hota,
        // toh filter chain ko aage badhao (bina authentication ke)  aur agle filter
        // ke pass jane do (shayad public API ho)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Yahan return karna zaroori hai taake baaki code execute na ho
        }

        // ==========================================
        // BOX 2: Token Extraction ("Bearer" ko kat kar sirf token nikalo)
        // ==========================================

        // "Bearer " ko kat kar sirf token nikalo (index 7 se aage sab token hai)
        final String token = authHeader.substring(7);

        // ==========================================
        // BOX 3: Validation (Jwt Service ko call)
        // ==========================================

        try {
            // 0. Email nikalo aur check karo ke banda pehle se VIP toh nahi? (Double Entry Check)
            final String email = jwtService.extractEmail(token);
            // Agar email mil gaya aur Security Context mein pehle se koi authentication hai,
            // toh iska matlab hai ke yeh banda pehle se VIP list mein hai,
            // toh dobara token validate karne ki zaroorat nahi hai, aage badho
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 1. Token valid hai ya nahi?
                if (jwtService.isTokenValid(token)) {  // agar valid he to
                    // 2. Token se Email, Role aur UserId nikalo
                    String role = jwtService.extractRole(token);
                    String userId = jwtService.extractUserId(token);

                    log.debug("JWT Vlaid - Email: {}, Role: {}", email, role);


                    // ==========================================
                    // BOX 4: Security Context (VIP list mein naam likhna)
                    // ==========================================
                    // Spring Security ko batao ke yeh user authenticated hai,
                    // uska email, role aur userId kya hai
                    // Taake Spring Security ke filters aur controllers me yeh
                    // information available ho
                    // 1. Authentication object banao (UsernamePasswordAuthenticationToken)
                    // Isme hum Email (principal), Password (null rakhte hain kyunke token verify ho chuka hai),
                    // aur Role (authorities) pass karte hain.
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, // Principal — kaun hai?
                            null, // Credentials — password nahi chahiye ab
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)) // Authorities —
                            // role ko "ROLE_" prefix ke saath dena hota hai

                    );
                    // Request ki details authentication mein add karo (optional, lekin useful hota hai)
                    // Yeh line request ki details (jaise IP address) token ke sath attach kar deti hai
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 3. UserId ko request header mein daalo
                    //    (Controller mein @RequestAttribute("X-User-Id") se milega)
                    request.setAttribute("X-User-Id", userId);


                    // 4. SecurityContextHolder me Authentication object set/save karo (VIP list mein naam likhna)
                    // Taake Spring Security ke filters aur controllers me yeh information available ho
                    // yaha par Spring Security ko batayenge ke yeh banda verified hai
                    SecurityContextHolder.getContext().setAuthentication(authentication);


                } else {
                    // Agar token expire hai ya kharab hai, toh error log karo aur jane do.
                    // Aage Spring Security khud 401 de dega.
                    log.warn("JWT Validation failed: Token is invalid or expired");
                }
            }

        } catch (Exception e){
            // Agar token ka structure kharab hai toh crash hone ke bajaye yahan catch hoga
            log.error("JWT Parsing Error {}", e.getMessage());
        }


        // ==========================================
        // THE END: Aage bhej do
        // ==========================================
        // Uske baad hum isko aage bhej denge Spring Security Filter Chain (Agle Filters) ki taraf
        filterChain.doFilter(request, response);

    }
}
