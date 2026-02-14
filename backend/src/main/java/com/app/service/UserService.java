package com.app.service;

import com.app.dto.UserDto;
import com.app.mapper.DtoMapper;
import com.app.model.User;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DtoMapper mapper;

    public User getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public UserDto getById(Long id) {
        return mapper.toUserDto(getEntity(id));
    }

    @Transactional
    public void setOnline(Long userId, boolean online) {
        User u = getEntity(userId);
        u.setOnline(online);
        u.setLastSeen(Instant.now());
        userRepository.save(u);
    }

    public List<UserDto> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> listAllExcept(Long excludeUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByIdNot(excludeUserId, pageable)
                .stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    /** Retourne tous les utilisateurs de la base (pour affichage liste amis). */
    public List<UserDto> listAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 500));
        return userRepository.findAll(pageable).getContent().stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }
}
