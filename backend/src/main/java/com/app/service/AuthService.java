package com.app.service;

import com.app.dto.AuthRequest;
import com.app.dto.AuthResponse;
import com.app.dto.RegisterRequest;
import com.app.model.User;
import com.app.repository.UserRepository;
import com.app.security.JwtUtil;
import com.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email déjà utilisé");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("Nom d'utilisateur déjà utilisé");
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.create(user);
        String token = jwtUtil.generateToken(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtUtil.generateToken(auth);
        return AuthResponse.builder()
                .token(token)
                .id(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .role(principal.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .orElse("USER"))
                .build();
    }
}
