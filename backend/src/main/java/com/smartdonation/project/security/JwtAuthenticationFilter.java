package com.smartdonation.project.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/login"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Skip JWT validation for public endpoints.
        if (PUBLIC_PATHS.stream().anyMatch(path -> request.getServletPath().startsWith(path))) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.retrieveTokenFromRequest(request);
        try {
            if (token == null)
                throw new IllegalArgumentException("Authorization header is empty");

            String email = jwtUtil.retrieveEmailFromToken(token);
            List<String> roles = jwtUtil.retrieveRolesFromToken(token);

            List<GrantedAuthority> authorities =
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .map(authority ->
                                    (GrantedAuthority) authority)
                            .toList();

            /*g
                Create Authentication Object
             */

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            log.info("Authentication successful");
            log.info("Email : " + email);
            log.info("Roles : " + roles);

        }
        catch (IllegalArgumentException e) {
            log.error("Authentication failed : {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                        {
                            "message":"Authorization header is empty"
                        }
                    """);
            return;
        }
        catch (JWTVerificationException e) {
            log.error("Invalid JWT token : {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                        {
                            "message":"Invalid Authorization token"
                        }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }



}
