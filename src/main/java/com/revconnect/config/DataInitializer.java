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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.revconnect.repository.UserRepository;

@Component
@Profile("!test")
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
                if (userRepository.count() > 0)
                        return;

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
                User bob = registerUser("bob", "bob@example.com", "password", "Bob Smith", UserRole.PERSONAL);
                User carol = registerUser("carol", "carol@example.com", "password", "Carol White", UserRole.PERSONAL);

                // Creator
                User creator = registerUser("techcreator", "creator@example.com", "password", "Tech Vibes",
                                UserRole.CREATOR);
                creator.setCategory("Technology");
                creator.setBio("Sharing the latest in tech & innovation 🚀");
                userRepository.save(creator);

                // Business
                User business = registerUser("shopnow", "shop@example.com", "password", "ShopNow Store",
                                UserRole.BUSINESS);
                business.setCategory("E-Commerce");
                business.setBio("Your one-stop shop for everything!");
                business.setBusinessAddress("123 Commerce St, NY");
                business.setBusinessHours("Mon-Fri 9AM-6PM");
                userRepository.save(business);

                // Posts
                createPost(alice, "Hey everyone! Just joined RevConnect 👋 Excited to connect!", "hello,introduction",
                                PostType.REGULAR);
                createPost(alice, "Beautiful morning today. Grateful for every moment. ☀️", "morning,gratitude",
                                PostType.REGULAR);
                createPost(bob, "Just finished reading an amazing book on productivity. Highly recommend!",
                                "books,productivity", PostType.REGULAR);
                createPost(bob, "Working on a new project. Can't share details yet but it's exciting! 🔥",
                                "project,work",
                                PostType.REGULAR);
                createPost(carol, "Spring is here! Time to go outside and enjoy the sunshine 🌸", "spring,nature",
                                PostType.REGULAR);
                createPost(creator, "🚀 Top 5 AI tools you NEED in 2024. Thread below 👇", "ai,technology,tools",
                                PostType.REGULAR);
                createPost(creator, "Hot take: React is still the best frontend framework. Change my mind 💬",
                                "react,javascript,webdev", PostType.REGULAR);
                createPost(business, "🛍️ MEGA SALE — Up to 50% off this weekend only!", "sale,deals,shopping",
                                PostType.PROMOTIONAL);
                createPost(business, "Introducing our NEW Summer Collection! Shop now 🌊", "new,summer,fashion,premium",
                                PostType.PROMOTIONAL);

                // Targeted Hashtag Posts
                createPost(creator,
                                "Innovation is not just about technology; it's about a new way of seeing the world. 💡",
                                "innovation,tech,future", PostType.REGULAR);
                createPost(alice, "Obsessed with the new #lavender theme on RevConnect! It looks so premium. ✨",
                                "lavender,revconnect,premium", PostType.REGULAR);
                createPost(bob, "RevConnect is changing the game for social media. Clean UI, great features! 🚀",
                                "revconnect,socialmedia,ux", PostType.REGULAR);
                createPost(business, "Scale your business effortlessly with our new SaaS integration tools. 📈",
                                "saas,business,productivity", PostType.PROMOTIONAL);
                createPost(creator, "The future of social media is here. Connect deeply, create freely. 🌐",
                                "socialmedia,innovation,revconnect", PostType.REGULAR);
                createPost(alice, "Just discovered the #premium features of RevConnect. Definitely worth it! 💎",
                                "premium,revconnect,deals", PostType.REGULAR);
                createPost(bob, "Building the next big thing in SaaS. Stay tuned! 🏗️", "saas,innovation,startup",
                                PostType.REGULAR);
                createPost(carol, "This #lavender aesthetic is everything. My feed looks so much better now. 💜",
                                "lavender,aesthetic,revconnect", PostType.REGULAR);

                // More Diverse Seed Data
                createPost(creator, "Just dropped a new video on #Web3 and its impact on social networking! 🎥",
                                "web3,crypto,future", PostType.REGULAR);
                createPost(business, "Don't miss our #flashsale starting in 1 hour! Get ready! ⚡",
                                "flashsale,deals,shopping", PostType.PROMOTIONAL);
                createPost(alice, "Learning #React today. Any tips for beginners? 💻",
                                "react,webdev,learning", PostType.REGULAR);
                createPost(bob, "The #AI revolution is just beginning. What a time to be alive! 🤖",
                                "ai,tech,innovation", PostType.REGULAR);
                createPost(carol, "Fresh flowers from the garden today. #nature is beautiful 🌸",
                                "nature,garden,peace", PostType.REGULAR);
                createPost(creator, "Why #UX matters more than ever in 2024. Let's discuss! 🎨",
                                "ux,design,innovation", PostType.REGULAR);
                createPost(business, "Our new office is finally ready! Come say hi 🏢 #business #growth",
                                "business,growth,newworkspace", PostType.PROMOTIONAL);
                createPost(alice, "Working out every day makes such a difference. #health #fitness 🏃‍♀️",
                                "health,fitness,mindset", PostType.REGULAR);
                createPost(bob, "The #future of work is remote. Agree or disagree? 🏠",
                                "future,remote,worklife", PostType.REGULAR);
                createPost(carol, "Made some #homemade pasta today. It was delicious! 🍝",
                                "food,cooking,delicious", PostType.REGULAR);
                createPost(creator, "Exploring the possibilities of #AR in everyday life. Mind-blown! 🕶️",
                                "ar,future,tech", PostType.REGULAR);
                createPost(business, "We've empowered over 1000 creators this year! 🚀 #impact #creators",
                                "impact,creators,business", PostType.PROMOTIONAL);
                createPost(alice, "Can't wait for the weekend! Any plans? 🎈",
                                "weekend,fun,lifestyle", PostType.REGULAR);
                createPost(bob, "Reading about #sustainability today. What are you doing for the planet? 🌍",
                                "sustainability,eco,future", PostType.REGULAR);
                createPost(carol, "This #revconnect platform is so intuitive and looks amazing. Great job team! 👏",
                                "revconnect,ux,design", PostType.REGULAR);

                // Additional Connections
                try {
                        var req = connectionService.sendRequest(alice, bob);
                        connectionService.acceptRequest(req.getId(), bob);

                        var req2 = connectionService.sendRequest(carol, alice);
                        connectionService.acceptRequest(req2.getId(), alice);

                        var req3 = connectionService.sendRequest(bob, carol); // Correcting case

                        if (req3 != null)
                                connectionService.acceptRequest(req3.getId(), carol);
                } catch (Exception e) {
                        log.warn("Could not create connections: {}", e.getMessage());
                }

                // More follows to ensure rich feed
                userService.follow(carol, creator);
                userService.follow(business, creator);
                userService.follow(bob, business);
                userService.follow(carol, business);

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
                                .securityQuestion("Default Question")
                                .securityAnswer("Default Answer")
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
