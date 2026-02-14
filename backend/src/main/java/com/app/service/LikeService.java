package com.app.service;

import com.app.model.Like;
import com.app.model.Notification;
import com.app.model.Post;
import com.app.model.User;
import com.app.repository.LikeRepository;
import com.app.mapper.DtoMapper;
import com.app.repository.NotificationRepository;
import com.app.repository.PostRepository;
import com.app.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserService userService;
    private final DtoMapper mapper;

    @Transactional
    public void toggle(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publication non trouvée"));
        User user = userService.getEntity(userId);
        var existing = likeRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return;
        }
        Like like = Like.builder().user(user).post(post).build();
        likeRepository.save(like);
        if (!post.getAuthor().getId().equals(userId)) {
            Notification notif = Notification.builder()
                    .type(Notification.NotificationType.LIKE)
                    .message(user.getUsername() + " a aimé votre publication.")
                    .user(post.getAuthor())
                    .actorId(userId)
                    .postId(postId)
                    .build();
            notif = notificationRepository.save(notif);
            webSocketNotificationService.sendToUser(post.getAuthor().getId(), mapper.toNotificationDto(notif));
        }
    }
}
