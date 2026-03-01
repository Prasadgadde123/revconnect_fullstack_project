package com.revconnect.controller;

import com.revconnect.model.user.User;
import com.revconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserTestController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public String createTestUser() {
        User user = new User("testuser", "test@email.com", "password123");
        userRepository.save(user);
        return "Test user created!";
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/count")
    public String getUserCount() {
        long count = userRepository.count();
        return "Total users: " + count;
    }
}