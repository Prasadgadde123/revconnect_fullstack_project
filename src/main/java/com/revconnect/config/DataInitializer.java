package com.revconnect.config;

import com.revconnect.dto.PostCreateDTO;
import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.User;
import com.revconnect.enums.PostType;
import com.revconnect.enums.UserRole;
import com.revconnect.service.ConnectionService;
import com.revconnect.service.PostService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.revconnect.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PostService postService;
    private final ConnectionService connectionService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Seeding demo data...");

        // Admin
        User admin = com.revconnect.entity.User.builder()
                .username("admin")
                .email("admin@revconnect.com")
                .password(passwordEncoder.encode("admin123"))
                .displayName("Admin")
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);

        // Personal users
        User alice = registerUser("alice", "alice@example.com", "password", "Alice Johnson", UserRole.PERSONAL);
        User bob   = registerUser("bob",   "bob@example.com",   "password", "Bob Smith",    UserRole.PERSONAL);
        User carol = registerUser("carol", "carol@example.com", "password", "Carol White",  UserRole.PERSONAL);

        // Creator
        User creator = registerUser("techcreator", "creator@example.com", "password", "Tech Vibes", UserRole.CREATOR);
        creator.setCategory("Technology");
        creator.setBio("Sharing the latest in tech & innovation 🚀");
        userRepository.save(creator);

        // Business
        User business = registerUser("shopnow", "shop@example.com", "password", "ShopNow Store", UserRole.BUSINESS);
        business.setCategory("E-Commerce");
        business.setBio("Your one-stop shop for everything!");
        business.setBusinessAddress("123 Commerce St, NY");
        business.setBusinessHours("Mon-Fri 9AM-6PM");
        userRepository.save(business);

        // Posts
        createPost(alice, "Hey everyone! Just joined RevConnect 👋 Excited to connect!", "hello,introduction", PostType.REGULAR);
        createPost(alice, "Beautiful morning today. Grateful for every moment. ☀️", "morning,gratitude", PostType.REGULAR);
        createPost(bob, "Just finished reading an amazing book on productivity. Highly recommend!", "books,productivity", PostType.REGULAR);
        createPost(bob, "Working on a new project. Can't share details yet but it's exciting! 🔥", "project,work", PostType.REGULAR);
        createPost(carol, "Spring is here! Time to go outside and enjoy the sunshine 🌸", "spring,nature", PostType.REGULAR);
        createPost(creator, "🚀 Top 5 AI tools you NEED in 2024. Thread below 👇", "ai,technology,tools", PostType.REGULAR);
        createPost(creator, "Hot take: React is still the best frontend framework. Change my mind 💬", "react,javascript,webdev", PostType.REGULAR);
        createPost(business, "🛍️ MEGA SALE — Up to 50% off this weekend only!", "sale,deals,shopping", PostType.PROMOTIONAL);
        createPost(business, "Introducing our NEW Summer Collection! Shop now 🌊", "new,summer,fashion", PostType.PROMOTIONAL);

        // Connections
        try {
            var req = connectionService.sendRequest(alice, bob);
            connectionService.acceptRequest(req.getId(), bob);

            var req2 = connectionService.sendRequest(carol, alice);
            connectionService.acceptRequest(req2.getId(), alice);
        } catch (Exception e) {
            log.warn("Could not create connections: {}", e.getMessage());
        }

        // Follows
        userService.follow(alice, creator);
        userService.follow(bob, creator);
        userService.follow(carol, business);
        userService.follow(alice, business);

        log.info("Demo data seeded successfully!");
        log.info("Login: alice/password, bob/password, carol/password");
        log.info("Creator: techcreator/password, Business: shopnow/password");
        log.info("Admin: admin/admin123");
    }

    private User registerUser(String username, String email, String password, String displayName, UserRole role) {
        RegisterDTO dto = RegisterDTO.builder()
                .username(username)
                .email(email)
                .password(password)
                .confirmPassword(password)
                .displayName(displayName)
                .role(role)
                .build();
        return userService.register(dto);
    }

    private void createPost(User author, String content, String hashtags, PostType type) {
        PostCreateDTO dto = PostCreateDTO.builder()
                .content(content)
                .hashtags(hashtags)
                .postType(type)
                .build();
        postService.createPost(author, dto);
    }
}
