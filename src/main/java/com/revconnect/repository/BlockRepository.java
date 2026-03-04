// src/main/java/com/revconnect/repository/BlockRepository.java
package com.revconnect.repository;

import com.revconnect.model.user.Block;
import com.revconnect.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    @Query("SELECT b.blocked FROM Block b WHERE b.blocker.id = :userId")
    List<User> findBlockedUsers(@Param("userId") Long userId);

    @Query("SELECT b.blocker FROM Block b WHERE b.blocked.id = :userId")
    List<User> findBlockedByUsers(@Param("userId") Long userId);

    void deleteByBlockerAndBlocked(User blocker, User blocked);
}