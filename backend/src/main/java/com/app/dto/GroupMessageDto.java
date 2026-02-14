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
public class GroupMessageDto {
    private Long id;
    private String content;
    private Long groupId;
    private UserDto sender;
    private Instant createdAt;
}
