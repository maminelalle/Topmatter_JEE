package com.app.controller;

import com.app.dto.UserDto;
import com.app.security.UserPrincipal;
import com.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.getById(userId));
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.listAllExcept(userId, page, size));
    }

    /** Liste de tous les utilisateurs (depuis la BDD). Authentification requise. */
    @GetMapping("/list-all")
    public ResponseEntity<List<UserDto>> listAllFromDb(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (getCurrentUserId() == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.listAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> search(@RequestParam String q) {
        return ResponseEntity.ok(userService.search(q));
    }

    @PatchMapping("/me/online")
    public ResponseEntity<Void> setOnline(@RequestParam boolean online) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        userService.setOnline(userId, online);
        return ResponseEntity.ok().build();
    }
}
