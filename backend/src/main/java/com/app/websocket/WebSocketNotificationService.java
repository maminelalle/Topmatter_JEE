package com.app.websocket;

import com.app.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component("webSocketNotificationService")
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(Long userId, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", notification);
    }

    public void sendToUser(Long userId, Object payload) {
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", payload);
    }
}
