package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    private String content;
    private String imageUrl;
    private UserDto author;
    private Instant createdAt;
    private Instant updatedAt;
    private long likeCount;
    private long commentCount;
    private Boolean likedByCurrentUser;
    private String visibility;
    private Long groupId;
    private String groupName;
}
