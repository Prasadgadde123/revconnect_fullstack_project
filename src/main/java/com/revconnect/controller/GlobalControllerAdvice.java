package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final com.revconnect.repository.PostRepository postRepository;
    private final com.revconnect.repository.ConnectionRepository connectionRepository;

    @ModelAttribute
    public void addGlobalAttributes(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser != null) {
            // Re-fetch or provide counts to avoid LazyInitializationException in
            // sidebar/fragments
            model.addAttribute("currentUserFollowerCount", userRepository.countFollowers(currentUser.getId()));
            model.addAttribute("currentUserFollowingCount", userRepository.countFollowing(currentUser.getId()));
            model.addAttribute("currentUserPostCount", postRepository.countByAuthorAndDeletedFalse(currentUser));

            // Connection status sets for universal button logic
            java.util.Set<Long> connectedIds = new java.util.HashSet<>();
            connectionRepository.findAcceptedConnections(currentUser).forEach(c -> {
                connectedIds.add(c.getRequester().getId());
                connectedIds.add(c.getReceiver().getId());
            });
            connectedIds.remove(currentUser.getId());
            model.addAttribute("globalConnectedIds", connectedIds);

            java.util.Set<Long> pendingSentIds = connectionRepository.findPendingSent(currentUser).stream()
                    .map(c -> c.getReceiver().getId())
                    .collect(java.util.stream.Collectors.toSet());
            model.addAttribute("globalPendingSentIds", pendingSentIds);

            java.util.Set<Long> followingIds = userRepository.findById(currentUser.getId())
                    .map(u -> u.getFollowing().stream().map(com.revconnect.entity.User::getId)
                            .collect(java.util.stream.Collectors.toSet()))
                    .orElse(new java.util.HashSet<>());
            model.addAttribute("globalFollowingIds", followingIds);

            model.addAttribute("currentUser", currentUser);
        }
    }
}
