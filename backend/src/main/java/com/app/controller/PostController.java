package com.app.controller;

import com.app.dto.PostDto;
import com.app.security.UserPrincipal;
import com.app.service.FileStorageService;
import com.app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<PostDto>> getTimeline(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(postService.getTimeline(userId, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(postService.searchPosts(q, userId, page, size));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        try {
            String imageUrl = fileStorageService.storeImage(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erreur lors de l'enregistrement du fichier."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(postService.getById(id, userId));
    }

    @PostMapping
    public ResponseEntity<PostDto> create(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        String content = body.get("content") != null ? body.get("content").toString() : null;
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().build();
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        String visibility = body.get("visibility") != null ? body.get("visibility").toString() : "PUBLIC";
        Long groupId = null;
        if (body.get("groupId") != null && !body.get("groupId").toString().isBlank()) {
            groupId = body.get("groupId") instanceof Number ? ((Number) body.get("groupId")).longValue() : Long.valueOf(body.get("groupId").toString());
        }
        return ResponseEntity.ok(postService.create(content, imageUrl, userId, visibility, groupId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        String content = (String) body.get("content");
        String imageUrl = (String) body.get("imageUrl");
        String visibility = (String) body.get("visibility");
        Long groupId = body.get("groupId") != null ? Long.valueOf(body.get("groupId").toString()) : null;
        return ResponseEntity.ok(postService.update(id, content, imageUrl, visibility, groupId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        postService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
