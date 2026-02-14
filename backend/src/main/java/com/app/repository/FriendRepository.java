package com.app.repository;

import com.app.model.Friend;
import com.app.model.Friend.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserIdAndStatus(Long userId, FriendStatus status);
    List<Friend> findByFriendIdAndStatus(Long friendId, FriendStatus status);
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
