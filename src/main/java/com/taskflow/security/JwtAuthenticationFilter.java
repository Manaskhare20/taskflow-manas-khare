package com.taskflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            log.warn("action=jwt_rejected reason=empty_bearer path={}", request.getRequestURI());
            writeUnauthorized(response);
            return;
        }
        try {
            var claims = jwtService.parseClaims(token);
            String userIdStr = claims.get("user_id", String.class);
            if (userIdStr == null) {
                userIdStr = claims.getSubject();
            }
            String email = claims.get("email", String.class);
            if (userIdStr == null || email == null) {
                log.warn("action=jwt_rejected reason=missing_claims path={}", request.getRequestURI());
                writeUnauthorized(response);
                return;
            }
            UUID userId = UUID.fromString(userIdStr);
            var principal = new AuthenticatedUser(userId, email);
            var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("action=jwt_accepted userId={} path={}", userId, request.getRequestURI());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn(
                    "action=jwt_rejected reason=invalid_token path={} detail={}",
                    request.getRequestURI(),
                    ex.getClass().getSimpleName());
            writeUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ErrorResponse.builder().error("unauthenticated").fields(Map.of()).build();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
