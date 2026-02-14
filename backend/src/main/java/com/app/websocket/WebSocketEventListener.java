package com.app.websocket;

import com.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor header = StompHeaderAccessor.wrap(event.getMessage());
        String userId = header.getSessionAttributes() != null ? (String) header.getSessionAttributes().get("userId") : null;
        if (userId != null) {
            try {
                userService.setOnline(Long.parseLong(userId), false);
                messagingTemplate.convertAndSend("/topic/online", userId);
            } catch (Exception ignored) {}
        }
    }
}
