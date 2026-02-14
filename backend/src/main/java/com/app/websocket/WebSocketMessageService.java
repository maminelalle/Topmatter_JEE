package com.app.websocket;

import com.app.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component("webSocketMessageService")
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(Long receiverId, MessageDto message) {
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/messages", message);
    }
}
