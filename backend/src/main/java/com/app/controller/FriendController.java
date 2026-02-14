package com.app.controller;

import com.app.dto.FriendDto;
import com.app.dto.UserDto;
import com.app.service.FriendService;
import com.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getFriends() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendDto>> getPendingRequests() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(friendService.getPendingRequests(userId));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<UserDto>> getSentRequests() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(friendService.getSentRequests(userId));
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        Object fid = body.get("friendId");
        if (fid == null) return ResponseEntity.badRequest().body(Map.of("message", "friendId requis"));
        Long friendId;
        try {
            friendId = fid instanceof Number ? ((Number) fid).longValue() : Long.valueOf(fid.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "friendId invalide"));
        }
        try {
            friendService.sendRequest(userId, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/requests/sent/{friendId}")
    public ResponseEntity<Void> cancelSentRequest(@PathVariable Long friendId) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        friendService.cancelSentRequest(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long requestId) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        friendService.acceptRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long requestId) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        friendService.rejectRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> unfriend(@PathVariable Long friendId) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        friendService.unfriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
