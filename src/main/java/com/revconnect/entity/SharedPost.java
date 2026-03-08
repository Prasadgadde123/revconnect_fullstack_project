package com.revconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SharedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private String message;

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime sharedAt = LocalDateTime.now();
}
