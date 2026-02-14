package com.app.service;

import com.app.dto.FriendDto;
import com.app.dto.UserDto;
import com.app.mapper.DtoMapper;
import com.app.model.Friend;
import com.app.model.Notification;
import com.app.model.User;
import com.app.repository.FriendRepository;
import com.app.repository.NotificationRepository;
import com.app.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserService userService;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<UserDto> getFriends(Long userId) {
        return friendRepository.findByUserIdAndStatus(userId, Friend.FriendStatus.ACCEPTED)
                .stream()
                .map(f -> mapper.toUserDto(f.getFriend()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendDto> getPendingRequests(Long userId) {
        return friendRepository.findByFriendIdAndStatus(userId, Friend.FriendStatus.PENDING)
                .stream()
                .map(f -> FriendDto.builder()
                        .id(f.getId())
                        .user(mapper.toUserDto(f.getUser()))
                        .status(f.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getSentRequests(Long userId) {
        return friendRepository.findByUserIdAndStatus(userId, Friend.FriendStatus.PENDING)
                .stream()
                .map(f -> mapper.toUserDto(f.getFriend()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) throw new IllegalArgumentException("Impossible d'ajouter vous-même.");
        if (friendRepository.existsByUserIdAndFriendId(fromUserId, toUserId))
            return;
        User from = userService.getEntity(fromUserId);
        User to = userService.getEntity(toUserId);
        Friend f = Friend.builder().user(from).friend(to).status(Friend.FriendStatus.PENDING).build();
        friendRepository.save(f);
        Notification notif = Notification.builder()
                .type(Notification.NotificationType.FRIEND_REQUEST)
                .message(from.getUsername() + " vous a envoyé une demande d'ami.")
                .user(to)
                .actorId(fromUserId)
                .build();
        notif = notificationRepository.save(notif);
        webSocketNotificationService.sendToUser(toUserId, mapper.toNotificationDto(notif));
    }

    @Transactional
    public void acceptRequest(Long requestId, Long userId) {
        Friend f = friendRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Demande non trouvée"));
        if (!f.getFriend().getId().equals(userId)) throw new IllegalArgumentException("Non autorisé");
        f.setStatus(Friend.FriendStatus.ACCEPTED);
        friendRepository.save(f);
        Friend reverse = Friend.builder().user(f.getFriend()).friend(f.getUser()).status(Friend.FriendStatus.ACCEPTED).build();
        friendRepository.save(reverse);
        Notification notif = Notification.builder()
                .type(Notification.NotificationType.FRIEND_ACCEPTED)
                .message(f.getFriend().getUsername() + " a accepté votre demande d'ami.")
                .user(f.getUser())
                .actorId(userId)
                .build();
        notif = notificationRepository.save(notif);
        webSocketNotificationService.sendToUser(f.getUser().getId(), mapper.toNotificationDto(notif));
    }

    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        Friend f = friendRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Demande non trouvée"));
        if (!f.getFriend().getId().equals(userId)) throw new IllegalArgumentException("Non autorisé");
        friendRepository.delete(f);
    }

    @Transactional
    public void cancelSentRequest(Long userId, Long friendId) {
        friendRepository.findByUserIdAndFriendId(userId, friendId)
                .filter(f -> f.getStatus() == Friend.FriendStatus.PENDING)
                .ifPresent(friendRepository::delete);
    }

    @Transactional
    public void unfriend(Long userId, Long friendId) {
        friendRepository.findByUserIdAndFriendId(userId, friendId)
                .ifPresent(friendRepository::delete);
        friendRepository.findByUserIdAndFriendId(friendId, userId)
                .ifPresent(friendRepository::delete);
    }
}
