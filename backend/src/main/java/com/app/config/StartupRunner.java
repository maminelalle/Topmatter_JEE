package com.app.config;

import com.app.model.User;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Au démarrage de l'application, déconnecte tous les utilisateurs (online = false)
 * pour forcer une reconnexion.
 */
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> all = userRepository.findAll();
        for (User u : all) {
            u.setOnline(false);
            userRepository.save(u);
        }
    }
}
