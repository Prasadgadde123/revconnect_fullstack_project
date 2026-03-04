package com.revconnect.security;

import com.revconnect.model.user.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.revconnect.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("========== LOAD USER BY USERNAME ==========");
        System.out.println("Looking for user: " + usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found with username or email: " + usernameOrEmail);
                    return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
                });

        System.out.println("✅ User found: " + user.getUsername());
        System.out.println("User password hash: " + user.getPassword());

        return UserDetailsImpl.build(user);
    }
}