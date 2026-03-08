package com.revconnect;

import com.revconnect.dto.PostCreateDTO;
import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.Post;
import com.revconnect.entity.User;
import com.revconnect.enums.PostType;
import com.revconnect.enums.UserRole;
import com.revconnect.exception.ResourceNotFoundException;
import com.revconnect.service.PostService;
import com.revconnect.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

    @Autowired private PostService postService;
    @Autowired private UserService userService;

    private User postAuthor;
    private User postOther;
    private User postAdmin;

    @BeforeEach
    void setUp() {
        postAuthor = userService.register(RegisterDTO.builder()
                .username("post_author").email("pauthor@test.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.PERSONAL).build());
        postOther = userService.register(RegisterDTO.builder()
                .username("post_other").email("pother@test.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.PERSONAL).build());
        postAdmin = userService.register(RegisterDTO.builder()
                .username("post_admin").email("padmin@test.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.ADMIN).build());
    }

    @Test
    void testCreatePost() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .content("Hello World!")
                .postType(PostType.REGULAR)
                .build();
        Post post = postService.createPost(postAuthor, dto);
        assertNotNull(post.getId());
        assertEquals("Hello World!", post.getContent());
    }

    @Test
    void testToggleLike() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Like me!").build());

        post = postService.toggleLike(post.getId(), postAuthor);
        assertEquals(1, post.getLikeCount());

        post = postService.toggleLike(post.getId(), postAuthor);
        assertEquals(0, post.getLikeCount());
    }

    @Test
    void testAddComment() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Post").build());
        var comment = postService.addComment(post.getId(), postAuthor, "Great post!");
        assertNotNull(comment.getId());
        assertEquals("Great post!", comment.getContent());
        assertEquals(1, postService.getComments(post.getId()).size());
    }

    @Test
    void testDeletePost_Owner() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Delete me").build());
        Long id = post.getId();
        postService.deletePost(id, postAuthor);
        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(id));
    }

    @Test
    void testUpdatePost_Success() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Original").build());
        PostCreateDTO updateDto = PostCreateDTO.builder().content("Updated").build();
        Post updated = postService.updatePost(post.getId(), postAuthor, updateDto);
        assertEquals("Updated", updated.getContent());
    }

    @Test
    void testUpdatePost_Unauthorized_Throws() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Original").build());
        PostCreateDTO updateDto = PostCreateDTO.builder().content("Updated").build();
        assertThrows(IllegalArgumentException.class, () -> postService.updatePost(post.getId(), postOther, updateDto));
    }

    @Test
    void testDeletePost_Unauthorized_Throws() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Stay").build());
        // postOther (non-admin) tries to delete postAuthor's post
        assertThrows(IllegalArgumentException.class, () -> postService.deletePost(post.getId(), postOther));
    }

    @Test
    void testRepost() {
        Post original = postService.createPost(postAuthor, PostCreateDTO.builder().content("Original").build());
        Post repost = postService.repost(original.getId(), postOther);
        assertNotNull(repost);
        assertEquals(PostType.REPOST, repost.getPostType());
        assertEquals(original.getId(), repost.getOriginalPost().getId());
    }

    @Test
    void testPinPost() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Pin me").build());
        assertFalse(post.isPinned());
        postService.togglePin(post.getId(), postAuthor);
        assertTrue(postService.getPostById(post.getId()).isPinned());
    }

    @Test
    void testTrendingHashtags() {
        postService.createPost(postAuthor, PostCreateDTO.builder().content("post 1").hashtags("java,tech").build());
        postService.createPost(postOther, PostCreateDTO.builder().content("post 2").hashtags("java,spring").build());
        Map<String, Long> trending = postService.getTrendingHashtags();
        assertTrue(trending.containsKey("java"));
        assertTrue(trending.get("java") >= 2);
    }

    @Test
    void testSearchByHashtag() {
        // Use a unique tag to avoid collisions with other tests
        postService.createPost(postAuthor, PostCreateDTO.builder().content("p1").hashtags("unique_java_test").build());
        postService.createPost(postOther, PostCreateDTO.builder().content("p2").hashtags("unique_spring_test").build());
        List<Post> results = postService.searchByHashtag("unique_java_test");
        assertEquals(1, results.size());
        assertEquals("p1", results.get(0).getContent());
    }

    @Test
    void testGetFeedPosts() {
        postService.createPost(postAuthor, PostCreateDTO.builder().content("Follow me").build());
        userService.follow(postOther, postAuthor);
        var feed = postService.getFeedPosts(postOther, 0, null, null);
        assertFalse(feed.getContent().isEmpty());
    }

    @Test
    void testDeleteComment_Unauthorized_Throws() {
        Post post = postService.createPost(postAuthor, PostCreateDTO.builder().content("Post").build());
        var comment = postService.addComment(post.getId(), postAuthor, "Hi");
        assertThrows(IllegalArgumentException.class, () -> postService.deleteComment(comment.getId(), postOther));
    }
}