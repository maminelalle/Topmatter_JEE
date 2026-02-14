package com.app.service;

import com.app.dto.MessageDto;
import com.app.dto.UserDto;
import com.app.mapper.DtoMapper;
import com.app.model.Message;
import com.app.model.Notification;
import com.app.model.User;
import com.app.repository.MessageRepository;
import com.app.repository.NotificationRepository;
import com.app.websocket.WebSocketMessageService;
import com.app.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger log = Logger.getLogger(MessageService.class.getName());
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final DtoMapper mapper;
    private final WebSocketMessageService webSocketMessageService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(Long user1Id, Long user2Id, int page, int size) {
        var messages = messageRepository.findConversation(user1Id, user2Id, PageRequest.of(page, size));
        return messages.stream().map(mapper::toMessageDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getConversationPartners(Long userId) {
        List<Long> partnerIds = messageRepository.findDistinctPartnerIdsByUserId(userId);
        List<UserDto> result = new ArrayList<>();
        for (Long id : partnerIds) {
            result.add(mapper.toUserDto(userService.getEntity(id)));
        }
        return result;
    }

    @Transactional
    public MessageDto send(Long senderId, Long receiverId, String content) {
        User sender = userService.getEntity(senderId);
        User receiver = userService.getEntity(receiverId);
        Message msg = Message.builder()
                .content(content)
                .sender(sender)
                .receiver(receiver)
                .build();
        msg = messageRepository.saveAndFlush(msg);
        MessageDto dto = mapper.toMessageDto(msg);
        try {
            webSocketMessageService.sendToUser(receiverId, dto);
            Notification notif = Notification.builder()
                    .type(Notification.NotificationType.MESSAGE)
                    .message(sender.getUsername() + " vous a envoyé un message.")
                    .user(receiver)
                    .actorId(senderId)
                    .build();
            notif = notificationRepository.save(notif);
            webSocketNotificationService.sendToUser(receiverId, mapper.toNotificationDto(notif));
        } catch (Exception e) {
            log.warning("Envoi notification/WebSocket message ignoré: " + e.getMessage());
        }
        return dto;
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            if (m.getReceiver().getId().equals(userId)) {
                m.setRead(true);
                messageRepository.save(m);
            }
        });
    }
}
