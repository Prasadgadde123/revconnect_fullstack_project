package com.revconnect.repository;

import com.revconnect.entity.SharedPost;
import com.revconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedPostRepository extends JpaRepository<SharedPost, Long> {

    /** Find all posts shared TO this user, newest first */
    List<SharedPost> findByRecipientOrderBySharedAtDesc(User recipient);

    /** Count unread shares for a user */
    long countByRecipientAndReadFalse(User recipient);
}
