package com.app.service;

import com.app.dto.CommentDto;
import com.app.mapper.DtoMapper;
import com.app.model.Comment;
import com.app.model.Notification;
import com.app.model.Post;
import com.app.model.User;
import com.app.repository.CommentRepository;
import com.app.repository.NotificationRepository;
import com.app.repository.PostRepository;
import com.app.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Logger log = Logger.getLogger(CommentService.class.getName());
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserService userService;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<CommentDto> getByPostId(Long postId) {
        var comments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);
        return comments.stream().map(mapper::toCommentDto).collect(Collectors.toList());
    }

    @Transactional
    public CommentDto create(Long postId, String content, Long userId, Long parentId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publication non trouvée"));
        User user = userService.getEntity(userId);
        Comment parent = parentId != null ? commentRepository.findById(parentId).orElse(null) : null;
        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .user(user)
                .parent(parent)
                .build();
        comment = commentRepository.save(comment);
        try {
            if (!post.getAuthor().getId().equals(userId)) {
                Notification notif = Notification.builder()
                        .type(Notification.NotificationType.COMMENT)
                        .message(user.getUsername() + " a commenté votre publication.")
                        .user(post.getAuthor())
                        .actorId(userId)
                        .postId(postId)
                        .build();
                notif = notificationRepository.save(notif);
                webSocketNotificationService.sendToUser(post.getAuthor().getId(), mapper.toNotificationDto(notif));
            }
        } catch (Exception e) {
            log.warning("Notification commentaire ignorée: " + e.getMessage());
        }
        return mapper.toCommentDto(comment);
    }

    @Transactional
    public void delete(Long commentId, Long currentUserId) {
        Comment c = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Commentaire non trouvé"));
        if (!c.getUser().getId().equals(currentUserId))
            throw new IllegalArgumentException("Non autorisé");
        commentRepository.delete(c);
    }
}
