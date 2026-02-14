package com.app.controller;

import com.app.dto.GroupDto;
import com.app.dto.GroupMessageDto;
import com.app.dto.PostDto;
import com.app.security.UserPrincipal;
import com.app.service.GroupMessageService;
import com.app.service.GroupService;
import com.app.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final PostService postService;
    private final GroupMessageService groupMessageService;

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<GroupDto> create(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        GroupDto dto = groupService.create(body.get("name"), body.get("description"), userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> getMyGroups() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(groupService.getMyGroups(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(groupService.getById(id, userId));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        Object uid = body.get("userId");
        Long memberId = uid instanceof Number ? ((Number) uid).longValue() : Long.valueOf(uid.toString());
        groupService.addMember(id, memberId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        groupService.leaveGroup(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<List<PostDto>> getGroupPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(postService.getGroupTimeline(id, userId, page, size));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<GroupMessageDto>> getGroupMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(groupMessageService.getMessages(id, userId, page, size));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<GroupMessageDto> sendGroupMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        String content = body.get("content") != null ? body.get("content").toString().trim() : "";
        if (content.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(groupMessageService.send(id, userId, content));
    }
}
