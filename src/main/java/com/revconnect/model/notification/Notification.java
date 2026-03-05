package com.revconnect.model.notification;

import com.revconnect.model.user.User;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;  // Who triggered the notification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "is_read")
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reference_id")
    private Long referenceId; // ID of post/comment/connection etc.

    @Column(name = "reference_type")
    private String referenceType; // "POST", "COMMENT", "CONNECTION", etc.
}

enum NotificationType {
    LIKE, COMMENT, SHARE, FOLLOW, CONNECTION_ACCEPTED,
    CONNECTION_REQUEST, POST_TAGGED, MENTION, BIRTHDAY,
    SYSTEM_ALERT, POST_SCHEDULED
}