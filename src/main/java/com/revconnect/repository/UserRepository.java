// src/main/java/com/revconnect/repository/UserRepository.java
package com.revconnect.repository;

import com.revconnect.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== BASIC FIND METHODS ==========

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // ========== BASIC SEARCH METHODS ==========

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    // ========== ENHANCED SEARCH METHODS WITH PAGINATION ==========

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchAllFields(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "u.userType = :userType AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchByUserType(@Param("query") String query,
                                @Param("userType") String userType,
                                Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "u.userType IN :userTypes AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchByUserTypes(@Param("query") String query,
                                 @Param("userTypes") List<String> userTypes,
                                 Pageable pageable);

    // ========== PRIVACY-AWARE SEARCH METHODS ==========

    @Query("SELECT u FROM User u WHERE u.isPrivate = false AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchPublicUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :currentUserId) " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchWithoutBlocked(@Param("query") String query,
                                    @Param("currentUserId") Long currentUserId,
                                    Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(u.isPrivate = false OR " +  // Public users
            "u.id = :currentUserId OR " +  // Own profile
            "EXISTS (SELECT f FROM Follow f WHERE f.follower.id = :currentUserId AND f.following.id = u.id)) " +  // Users I follow
            "AND u.id NOT IN (SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :currentUserId) " +  // Not blocked by me
            "AND u.id NOT IN (SELECT b.blocker.id FROM Block b WHERE b.blocked.id = :currentUserId) " +  // Not blocking me
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchVisibleUsers(@Param("query") String query,
                                  @Param("currentUserId") Long currentUserId,
                                  Pageable pageable);

    // ========== FOLLOWER/FOLLOWING RELATED SEARCHES ==========

    @Query("SELECT u FROM User u WHERE " +
            "EXISTS (SELECT f FROM Follow f WHERE f.follower.id = :currentUserId AND f.following.id = u.id) " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchFollowing(@Param("query") String query,
                               @Param("currentUserId") Long currentUserId,
                               Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "EXISTS (SELECT f FROM Follow f WHERE f.following.id = :currentUserId AND f.follower.id = u.id) " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchFollowers(@Param("query") String query,
                               @Param("currentUserId") Long currentUserId,
                               Pageable pageable);

    // ========== FILTER BY ENHANCED PROFILE FIELDS ==========

    Page<User> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    Page<User> findByOccupationContainingIgnoreCase(String occupation, Pageable pageable);

    Page<User> findByCompanyContainingIgnoreCase(String company, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    Page<User> findBySkill(@Param("skill") String skill, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.interests) LIKE LOWER(CONCAT('%', :interest, '%'))")
    Page<User> findByInterest(@Param("interest") String interest, Pageable pageable);

    Page<User> findByEducationContainingIgnoreCase(String education, Pageable pageable);

    // ========== COMBINED FILTERS ==========

    @Query("SELECT u FROM User u WHERE " +
            "(:query IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:userType IS NULL OR u.userType = :userType) AND " +
            "(:location IS NULL OR LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:occupation IS NULL OR LOWER(u.occupation) LIKE LOWER(CONCAT('%', :occupation, '%'))) AND " +
            "(:company IS NULL OR LOWER(u.company) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
            "(:education IS NULL OR LOWER(u.education) LIKE LOWER(CONCAT('%', :education, '%'))) AND " +
            "u.isPrivate = false")
    Page<User> advancedSearch(@Param("query") String query,
                              @Param("userType") String userType,
                              @Param("location") String location,
                              @Param("occupation") String occupation,
                              @Param("company") String company,
                              @Param("education") String education,
                              Pageable pageable);

    // ========== STATISTICS AND COUNTS ==========

    Long countByUserType(String userType);

    Long countByIsPrivate(Boolean isPrivate);

    Page<User> findByLastActiveAfterOrderByLastActiveDesc(LocalDateTime since, Pageable pageable);

    Page<User> findAllByOrderByProfileViewsDesc(Pageable pageable);

    // ========== BULK OPERATIONS ==========

    List<User> findByIdIn(List<Long> ids);

    List<User> findByUsernameIn(List<String> usernames);

    // ========== SUGGESTIONS / DISCOVERY ==========

    @Query("SELECT u FROM User u WHERE " +
            "u.id != :currentUserId AND " +
            "u.isPrivate = false AND " +
            "NOT EXISTS (SELECT f FROM Follow f WHERE f.follower.id = :currentUserId AND f.following.id = u.id) AND " +
            "NOT EXISTS (SELECT b FROM Block b WHERE b.blocker.id = :currentUserId AND b.blocked.id = u.id) AND " +
            "NOT EXISTS (SELECT b FROM Block b WHERE b.blocker.id = u.id AND b.blocked.id = :currentUserId) " +
            "ORDER BY u.profileViews DESC, u.lastActive DESC")
    Page<User> findSuggestions(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "u.id != :currentUserId AND " +
            "u.isPrivate = false AND " +
            "(:interests IS NULL OR " +
            "EXISTS (SELECT 1 FROM User u2 WHERE u2.id = u.id AND " +
            "LOWER(u2.interests) LIKE LOWER(CONCAT('%', :interest, '%')))) " +
            "ORDER BY u.profileViews DESC")
    Page<User> findBySimilarInterests(@Param("currentUserId") Long currentUserId,
                                      @Param("interests") String interests,
                                      Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.username = :username")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.email = :email")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchAllActiveFields(@Param("query") String query, Pageable pageable);
}