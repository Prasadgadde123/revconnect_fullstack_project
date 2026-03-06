
// src/main/java/com/revconnect/service/SearchService.java
package com.revconnect.service;

import com.revconnect.dto.request.UserSearchRequest;
import com.revconnect.dto.response.EnhancedProfileResponse;
import com.revconnect.dto.response.PageResponse;
import com.revconnect.model.user.User;
import com.revconnect.repository.BlockRepository;
import com.revconnect.repository.FollowRepository;
import com.revconnect.repository.UserRepository;
import com.revconnect.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final ProfileService profileService;

    public SearchService(UserRepository userRepository,
                         FollowRepository followRepository,
                         BlockRepository blockRepository,
                         ProfileService profileService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
        this.profileService = profileService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private Pageable createPageable(UserSearchRequest request) {
        Sort sort = request.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );
    }

    private PageResponse<EnhancedProfileResponse> toPageResponse(
            Page<User> userPage,
            User currentUser) {

        List<EnhancedProfileResponse> content = userPage.getContent().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))  // exclude own account
                .map(user -> profileService.mapToEnhancedProfileResponse(user, currentUser))
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    // ========== BASIC SEARCH ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchUsers(UserSearchRequest request) {
        User currentUser = getCurrentUser();
        Pageable pageable = createPageable(request);

        Page<User> users;

        if (request.getUserType() != null && !request.getUserType().isEmpty()) {
            // Search with user type filter
            users = userRepository.searchByUserType(
                    request.getQuery(),
                    request.getUserType(),
                    pageable
            );
        } else {
            // Basic search across all fields
            users = userRepository.searchAllFields(request.getQuery(), pageable);
        }

        return toPageResponse(users, currentUser);
    }

    // ========== PRIVACY-AWARE SEARCH ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchVisibleUsers(UserSearchRequest request) {
        User currentUser = getCurrentUser();
        Pageable pageable = createPageable(request);

        Page<User> users = userRepository.searchVisibleUsers(
                request.getQuery(),
                currentUser.getId(),
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchPublicUsers(UserSearchRequest request) {
        User currentUser = getCurrentUser();
        Pageable pageable = createPageable(request);

        Page<User> users = userRepository.searchPublicUsers(
                request.getQuery(),
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    // ========== FOLLOWER/FOLLOWING SEARCH ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchFollowing(UserSearchRequest request) {
        User currentUser = getCurrentUser();
        Pageable pageable = createPageable(request);

        Page<User> users = userRepository.searchFollowing(
                request.getQuery(),
                currentUser.getId(),
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchFollowers(UserSearchRequest request) {
        User currentUser = getCurrentUser();
        Pageable pageable = createPageable(request);

        Page<User> users = userRepository.searchFollowers(
                request.getQuery(),
                currentUser.getId(),
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    // ========== NEW METHODS FOR GETTING FOLLOWERS/FOLLOWING BY USERNAME ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> getFollowers(String username, UserSearchRequest request) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Pageable pageable = createPageable(request);
        Page<User> users = followRepository.findFollowerUsers(targetUser.getId(), pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> getFollowing(String username, UserSearchRequest request) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Pageable pageable = createPageable(request);
        Page<User> users = followRepository.findFollowingUsers(targetUser.getId(), pageable);

        return toPageResponse(users, currentUser);
    }

    // ========== ROLE-BASED FILTERING ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchByUserType(
            String query,
            List<String> userTypes,
            int page,
            int size) {

        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.searchByUserTypes(
                query,
                userTypes,
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    // ========== ADVANCED SEARCH WITH MULTIPLE FILTERS ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> advancedSearch(
            String query,
            String userType,
            String location,
            String occupation,
            String company,
            String education,
            int page,
            int size) {

        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.advancedSearch(
                query,
                userType,
                location,
                occupation,
                company,
                education,
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    // ========== SEARCH BY PROFILE FIELDS ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchByLocation(String location, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findByLocationContainingIgnoreCase(location, pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchByOccupation(String occupation, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findByOccupationContainingIgnoreCase(occupation, pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchByCompany(String company, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findByCompanyContainingIgnoreCase(company, pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchBySkill(String skill, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findBySkill(skill, pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchByInterest(String interest, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findByInterest(interest, pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> searchByEducation(String education, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findByEducationContainingIgnoreCase(education, pageable);

        return toPageResponse(users, currentUser);
    }

    // ========== USER SUGGESTIONS / DISCOVERY ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> getSuggestions(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findSuggestions(currentUser.getId(), pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> getSimilarInterests(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findBySimilarInterests(
                currentUser.getId(),
                currentUser.getInterests(),
                pageable
        );

        return toPageResponse(users, currentUser);
    }

    // ========== TRENDING / POPULAR USERS ==========

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> getMostFollowed(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("profileViews").descending());

        Page<User> users = userRepository.findAllByOrderByProfileViewsDesc(pageable);

        return toPageResponse(users, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnhancedProfileResponse> getRecentlyActive(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastActive").descending());

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<User> users = userRepository.findByLastActiveAfterOrderByLastActiveDesc(
                oneWeekAgo,
                pageable
        );

        return toPageResponse(users, currentUser);
    }
}