package com.revconnect.repository;

import com.revconnect.model.user.Follow;
import com.revconnect.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    Long countByFollower(User follower); // Following count

    Long countByFollowing(User following); // Followers count

    Page<Follow> findByFollower(User follower, Pageable pageable); // Who the user follows

    Page<Follow> findByFollowing(User following, Pageable pageable); // Who follows the user

    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    Page<User> findFollowingUsers(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    Page<User> findFollowerUsers(@Param("userId") Long userId, Pageable pageable);

    void deleteByFollowerAndFollowing(User follower, User following);
}