package com.app.service;

import com.app.dto.GroupMessageDto;
import com.app.mapper.DtoMapper;
import com.app.model.Group;
import com.app.model.GroupMessage;
import com.app.model.User;
import com.app.repository.GroupMemberRepository;
import com.app.repository.GroupMessageRepository;
import com.app.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMessageService {

    private final GroupMessageRepository groupMessageRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<GroupMessageDto> getMessages(Long groupId, Long currentUserId, int page, int size) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId))
            throw new IllegalArgumentException("Accès refusé à ce groupe");
        var messages = groupMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, PageRequest.of(page, size));
        return messages.stream().map(mapper::toGroupMessageDto).collect(Collectors.toList());
    }

    @Transactional
    public GroupMessageDto send(Long groupId, Long senderId, String content) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Groupe non trouvé"));
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, senderId))
            throw new IllegalArgumentException("Vous n'êtes pas membre de ce groupe");
        User sender = userService.getEntity(senderId);
        GroupMessage msg = GroupMessage.builder()
                .group(group)
                .sender(sender)
                .content(content)
                .build();
        msg = groupMessageRepository.save(msg);
        return mapper.toGroupMessageDto(msg);
    }
}
