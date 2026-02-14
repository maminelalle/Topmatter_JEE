package com.app.controller;

import com.app.dto.MessageDto;
import com.app.dto.UserDto;
import com.app.service.MessageService;
import com.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<UserDto>> getConversations() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(messageService.getConversationPartners(userId));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<MessageDto>> getConversation(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(messageService.getConversation(userId, otherUserId, page, size));
    }

    @PostMapping
    public ResponseEntity<?> send(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        Object r = body.get("receiverId");
        if (r == null) return ResponseEntity.badRequest().build();
        Long receiverId = r instanceof Number ? ((Number) r).longValue() : Long.valueOf(r.toString());
        String content = body.get("content") != null ? body.get("content").toString().trim() : "";
        if (content.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(messageService.send(userId, receiverId, content));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        messageService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }
}
