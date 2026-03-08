package com.revconnect.entity;

import com.revconnect.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String displayName;
    private String bio;
    private String profilePicture;
    private String location;
    private String website;

    // Multiple external links (title → url)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_links", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "link_entry")
    @Builder.Default
    private List<String> externalLinks = new ArrayList<>(); // Format: "Title::https://url"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.PERSONAL;

    @Builder.Default
    private boolean privateProfile = false;

    @Builder.Default
    private boolean enabled = true;

    // Business/Creator specific
    private String category;
    private String businessAddress;
    private String businessHours;
    private String contactEmail;
    private String contactPhone;

    // Security Questions
    private String securityQuestion;
    private String securityAnswer;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Relations
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Connection> sentConnections = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Connection> receivedConnections = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id"))
    @Builder.Default
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    // Notification preferences stored as simple flags
    @Builder.Default
    private boolean notifyConnectionRequests = true;
    @Builder.Default
    private boolean notifyLikes = true;
    @Builder.Default
    private boolean notifyComments = true;
    @Builder.Default
    private boolean notifyFollowers = true;
    @Builder.Default
    private boolean notifyShares = true;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }

    public String getDisplayNameOrUsername() {
        return (displayName != null && !displayName.isBlank()) ? displayName : username;
    }
}
