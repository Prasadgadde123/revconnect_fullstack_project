package com.revconnect.repository;

import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%',:q,'%')) " +
                        "OR LOWER(u.displayName) LIKE LOWER(CONCAT('%',:q,'%'))")
        List<User> searchUsers(@Param("q") String query);

        List<User> findByRole(UserRole role);

        List<User> findByRoleIn(List<UserRole> roles);

        @Query("SELECT u FROM User u WHERE u.role IN ('CREATOR','BUSINESS') ORDER BY SIZE(u.followers) DESC")
        List<User> findTopCreatorsAndBusinesses();

        /**
         * Loads the user AND their following set in a single join — avoids
         * LazyInitializationException
         */
        @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.following WHERE u.id = :id")
        Optional<User> findByIdWithFollowing(@Param("id") Long id);

        /** Check follow relationship without loading the lazy collection */
        @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.following f WHERE u.id = :followerId AND f.id = :followingId")
        boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

        /** Get followers list via query */
        @Query("SELECT u FROM User u JOIN u.following f WHERE f.id = :userId")
        List<User> findFollowers(@Param("userId") Long userId);

        /** Get following list via query */
        @Query("SELECT f FROM User u JOIN u.following f WHERE u.id = :userId")
        List<User> findFollowing(@Param("userId") Long userId);

        /** Count followers */
        @Query("SELECT COUNT(u) FROM User u JOIN u.following f WHERE f.id = :userId")
        long countFollowers(@Param("userId") Long userId);

        /** Count following */
        @Query("SELECT COUNT(f) FROM User u JOIN u.following f WHERE u.id = :userId")
        long countFollowing(@Param("userId") Long userId);

        @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.bookmarkedUsers WHERE u.id = :id")
        Optional<User> findByIdWithBookmarks(@Param("id") Long id);

        @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.bookmarkedUsers b WHERE u.id = :userId AND b.id = :bookmarkId")
        boolean isBookmarked(@Param("userId") Long userId, @Param("bookmarkId") Long bookmarkId);

        long countByRole(UserRole role);

        @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%',:q,'%')) " +
                        "OR LOWER(u.displayName) LIKE LOWER(CONCAT('%',:q,'%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))")
        List<User> adminSearchUsers(@Param("q") String query);
}
