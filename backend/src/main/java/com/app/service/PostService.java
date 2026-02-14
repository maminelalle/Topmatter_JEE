package com.app.service;

import com.app.dto.PostDto;
import com.app.mapper.DtoMapper;
import com.app.model.Post;
import com.app.model.User;
import com.app.repository.CommentRepository;
import com.app.repository.FriendRepository;
import com.app.repository.GroupMemberRepository;
import com.app.repository.LikeRepository;
import com.app.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final FriendRepository friendRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<PostDto> getTimeline(Long currentUserId, int page, int size) {
        List<Long> groupIds = groupMemberRepository.findByUserId(currentUserId).stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toList());
        int fetchSize = Math.max(size * 5, 500);
        Pageable pageable = PageRequest.of(0, fetchSize);
        List<Post> allRecent = postRepository.findAllOrderByCreatedAtDesc(pageable).getContent();
        List<Post> publicPosts = allRecent.stream()
                .filter(p -> p.getVisibility() == null || p.getVisibility() == Post.Visibility.PUBLIC)
                .collect(Collectors.toList());
        // Toujours inclure mes propres publications pour que l'utilisateur les voie
        List<Post> myPosts = postRepository.findByAuthorIdInOrderByCreatedAtDesc(List.of(currentUserId), PageRequest.of(0, 200)).getContent();
        List<Post> groupPosts = groupIds.isEmpty() ? List.of()
                : postRepository.findByGroupIdInOrderByCreatedAtDesc(groupIds, pageable).getContent();
        Set<Long> seenIds = new HashSet<>();
        List<Post> combined = new ArrayList<>();
        for (Post p : publicPosts) {
            if (seenIds.add(p.getId())) combined.add(p);
        }
        for (Post p : groupPosts) {
            if (seenIds.add(p.getId())) combined.add(p);
        }
        for (Post p : myPosts) {
            if (seenIds.add(p.getId())) combined.add(p);
        }
        combined = combined.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .distinct()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        return combined.stream()
                .map(p -> toPostDto(p, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostDto> getGroupTimeline(Long groupId, Long currentUserId, int page, int size) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId))
            throw new IllegalArgumentException("Accès refusé à ce groupe");
        Pageable pageable = PageRequest.of(page, size);
        var posts = postRepository.findByGroupIdInOrderByCreatedAtDesc(List.of(groupId), pageable).getContent();
        return posts.stream()
                .map(p -> toPostDto(p, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostDto> searchPosts(String query, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var posts = postRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(query, pageable);
        return posts.stream()
                .map(p -> toPostDto(p, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostDto getById(Long postId, Long currentUserId) {
        Post p = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publication non trouvée"));
        return toPostDto(p, currentUserId);
    }

    @Transactional
    public PostDto create(String content, String imageUrl, Long authorId, String visibility, Long groupId) {
        User author = userService.getEntity(authorId);
        Post.Visibility v = visibility != null && !visibility.isBlank()
                ? Post.Visibility.valueOf(visibility.toUpperCase())
                : Post.Visibility.PUBLIC;
        if (v == Post.Visibility.GROUP && (groupId == null || !groupMemberRepository.existsByGroupIdAndUserId(groupId, authorId)))
            throw new IllegalArgumentException("Groupe invalide ou vous n'êtes pas membre");
        Post post = Post.builder()
                .content(content)
                .imageUrl(imageUrl)
                .author(author)
                .visibility(v)
                .groupId(v == Post.Visibility.GROUP ? groupId : null)
                .build();
        post = postRepository.save(post);
        return toPostDto(post, authorId);
    }

    @Transactional
    public PostDto update(Long postId, String content, String imageUrl, String visibility, Long groupId, Long currentUserId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publication non trouvée"));
        if (!post.getAuthor().getId().equals(currentUserId))
            throw new IllegalArgumentException("Non autorisé");
        post.setContent(content != null ? content : post.getContent());
        if (imageUrl != null) post.setImageUrl(imageUrl);
        if (visibility != null && !visibility.isBlank()) {
            Post.Visibility v = Post.Visibility.valueOf(visibility.toUpperCase());
            post.setVisibility(v);
            post.setGroupId(v == Post.Visibility.GROUP ? groupId : null);
        }
        post = postRepository.save(post);
        return toPostDto(post, currentUserId);
    }

    @Transactional
    public void delete(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Publication non trouvée"));
        if (!post.getAuthor().getId().equals(currentUserId))
            throw new IllegalArgumentException("Non autorisé");
        postRepository.delete(post);
    }

    private PostDto toPostDto(Post p, Long currentUserId) {
        long likeCount = likeRepository.countByPostId(p.getId());
        long commentCount = commentRepository.countByPostId(p.getId());
        boolean liked = currentUserId != null && likeRepository.existsByUserIdAndPostId(currentUserId, p.getId());
        return mapper.toPostDto(p, currentUserId, likeCount, commentCount, liked);
    }
}
