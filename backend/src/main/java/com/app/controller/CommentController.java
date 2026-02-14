package com.app.controller;

import com.app.dto.CommentDto;
import com.app.security.UserPrincipal;
import com.app.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDto>> getByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getByPostId(postId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        Object pid = body.get("postId");
        if (pid == null) return ResponseEntity.badRequest().build();
        Long postId = pid instanceof Number ? ((Number) pid).longValue() : Long.valueOf(pid.toString());
        String content = body.get("content") != null ? body.get("content").toString().trim() : "";
        if (content.isEmpty()) return ResponseEntity.badRequest().build();
        Long parentId = null;
        if (body.get("parentId") != null) {
            Object p = body.get("parentId");
            parentId = p instanceof Number ? ((Number) p).longValue() : Long.valueOf(p.toString());
        }
        return ResponseEntity.ok(commentService.create(postId, content, userId, parentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        commentService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
