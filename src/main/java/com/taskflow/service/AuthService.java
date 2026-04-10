package com.taskflow.service;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.entity.User;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("action=register_attempt emailDomain={}", emailDomain(email));
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.warn("action=register_failed reason=email_taken emailDomain={}", emailDomain(email));
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "validation failed",
                    Map.of("email", "email is already registered"));
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("action=register_success userId={} emailDomain={}", user.getId(), emailDomain(email));
        return AuthResponse.builder().token(token).userId(user.getId()).email(user.getEmail()).build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("action=login_attempt emailDomain={}", emailDomain(email));
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("action=login_failed reason=user_not_found emailDomain={}", emailDomain(email));
                    return new BadCredentialsException("invalid credentials");
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("action=login_failed reason=bad_password userId={}", user.getId());
            throw new BadCredentialsException("invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("action=login_success userId={}", user.getId());
        return AuthResponse.builder().token(token).userId(user.getId()).email(user.getEmail()).build();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String emailDomain(String email) {
        int at = email.lastIndexOf('@');
        return at > 0 && at < email.length() - 1 ? email.substring(at + 1) : "unknown";
    }
}
