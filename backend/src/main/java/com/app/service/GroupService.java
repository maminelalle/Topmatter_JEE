package com.app.service;

import com.app.dto.GroupDto;
import com.app.dto.UserDto;
import com.app.mapper.DtoMapper;
import com.app.model.Group;
import com.app.model.GroupMember;
import com.app.model.User;
import com.app.repository.GroupMemberRepository;
import com.app.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final DtoMapper mapper;

    @Transactional
    public GroupDto create(String name, String description, Long userId) {
        User user = userService.getEntity(userId);
        Group group = Group.builder()
                .name(name)
                .description(description != null ? description : "")
                .createdBy(user)
                .build();
        group = groupRepository.save(group);
        GroupMember admin = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMember.Role.ADMIN)
                .build();
        groupMemberRepository.save(admin);
        return toGroupDto(group);
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getMyGroups(Long userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .map(gm -> toGroupDto(gm.getGroup()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDto getById(Long groupId, Long currentUserId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Groupe non trouvé"));
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId))
            throw new IllegalArgumentException("Accès refusé à ce groupe");
        return toGroupDto(group);
    }

    @Transactional
    public void addMember(Long groupId, Long userIdToAdd, Long currentUserId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Groupe non trouvé"));
        GroupMember current = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Vous n'êtes pas membre"));
        if (current.getRole() != GroupMember.Role.ADMIN)
            throw new IllegalArgumentException("Seul un admin peut ajouter des membres");
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userIdToAdd))
            throw new IllegalArgumentException("Déjà membre");
        User user = userService.getEntity(userIdToAdd);
        groupMemberRepository.save(GroupMember.builder().group(group).user(user).role(GroupMember.Role.MEMBER).build());
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember gm = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vous n'êtes pas membre"));
        if (gm.getRole() == GroupMember.Role.ADMIN) {
            long adminCount = groupMemberRepository.findByGroupId(groupId).stream()
                    .filter(m -> m.getRole() == GroupMember.Role.ADMIN).count();
            if (adminCount <= 1)
                throw new IllegalArgumentException("Le groupe doit avoir au moins un admin. Transférez le rôle avant de partir.");
        }
        groupMemberRepository.delete(gm);
    }

    public List<Long> getGroupIdsForUser(Long userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toList());
    }

    private GroupDto toGroupDto(Group g) {
        List<UserDto> members = groupMemberRepository.findByGroupId(g.getId()).stream()
                .map(gm -> mapper.toUserDto(gm.getUser()))
                .collect(Collectors.toList());
        return GroupDto.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .createdBy(mapper.toUserDto(g.getCreatedBy()))
                .createdAt(g.getCreatedAt())
                .members(members)
                .memberCount(members.size())
                .build();
    }
}
