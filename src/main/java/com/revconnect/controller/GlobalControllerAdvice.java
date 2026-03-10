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

    @ModelAttribute
    public void addGlobalAttributes(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser != null) {
            // Re-fetch or provide counts to avoid LazyInitializationException in sidebar/fragments
            model.addAttribute("currentUserFollowerCount", userRepository.countFollowers(currentUser.getId()));
            model.addAttribute("currentUserFollowingCount", userRepository.countFollowing(currentUser.getId()));
            model.addAttribute("currentUserPostCount", postRepository.countByAuthorAndDeletedFalse(currentUser));
        }
    }
}
