package com.app.mapper;

import com.app.dto.*;
import com.app.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public UserDto toUserDto(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .bio(u.getBio())
                .online(u.getOnline())
                .lastSeen(u.getLastSeen())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .build();
    }

    public PostDto toPostDto(Post p, Long currentUserId, long likeCount, long commentCount, boolean likedByCurrentUser) {
        if (p == null) return null;
        return PostDto.builder()
                .id(p.getId())
                .content(p.getContent())
                .imageUrl(p.getImageUrl())
                .author(toUserDto(p.getAuthor()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .likedByCurrentUser(likedByCurrentUser)
                .visibility(p.getVisibility() != null ? p.getVisibility().name() : Post.Visibility.PUBLIC.name())
                .groupId(p.getGroupId())
                .build();
    }

    public CommentDto toCommentDto(Comment c) {
        if (c == null) return null;
        return CommentDto.builder()
                .id(c.getId())
                .content(c.getContent())
                .user(toUserDto(c.getUser()))
                .postId(c.getPost().getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .createdAt(c.getCreatedAt())
                .replies(c.getReplies() != null ? c.getReplies().stream().map(this::toCommentDto).collect(Collectors.toList()) : null)
                .build();
    }

    public MessageDto toMessageDto(Message m) {
        if (m == null) return null;
        return MessageDto.builder()
                .id(m.getId())
                .content(m.getContent())
                .sender(toUserDto(m.getSender()))
                .receiverId(m.getReceiver().getId())
                .read(m.getRead())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public NotificationDto toNotificationDto(Notification n) {
        if (n == null) return null;
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .actorId(n.getActorId())
                .postId(n.getPostId())
                .read(n.getRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    public GroupMessageDto toGroupMessageDto(GroupMessage gm) {
        if (gm == null) return null;
        return GroupMessageDto.builder()
                .id(gm.getId())
                .content(gm.getContent())
                .groupId(gm.getGroup().getId())
                .sender(toUserDto(gm.getSender()))
                .createdAt(gm.getCreatedAt())
                .build();
    }
}
