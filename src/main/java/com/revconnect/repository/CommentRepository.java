package com.revconnect.repository;

import com.revconnect.entity.Comment;
import com.revconnect.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndDeletedFalseOrderByCreatedAtAsc(Post post);

    long countByPostAndDeletedFalse(Post post);

    long countByDeletedFalse();
}
