package com.app.security;

import com.app.model.User;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long id = Long.parseLong(username);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
            return UserPrincipal.create(user);
        } catch (NumberFormatException e) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByUsername(username).orElse(null));
            if (user == null) throw new UsernameNotFoundException("User not found: " + username);
            return UserPrincipal.create(user);
        }
    }
}
