package com.app.controller;

import com.app.security.UserPrincipal;
import com.app.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @PostMapping("/post/{postId}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long postId) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        likeService.toggle(postId, userId);
        return ResponseEntity.ok().build();
    }
}
