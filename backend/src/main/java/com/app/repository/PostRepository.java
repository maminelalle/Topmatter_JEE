package com.app.repository;

import com.app.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthorIdInOrderByCreatedAtDesc(List<Long> authorIds, Pageable pageable);
    Page<Post> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String query, Pageable pageable);
    Page<Post> findByVisibilityOrderByCreatedAtDesc(Post.Visibility visibility, Pageable pageable);
    Page<Post> findByGroupIdInOrderByCreatedAtDesc(List<Long> groupIds, Pageable pageable);

    /** Toutes les publications, triées par date (pour filtre public en Java si besoin). */
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);
}
