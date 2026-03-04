package com.revconnect.model.user;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})
})
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;  // The user who blocks

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;   // The user being blocked

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Block() {
        this.createdAt = LocalDateTime.now();
    }

    public Block(User blocker, User blocked) {
        this();
        this.blocker = blocker;
        this.blocked = blocked;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getBlocker() { return blocker; }
    public void setBlocker(User blocker) { this.blocker = blocker; }

    public User getBlocked() { return blocked; }
    public void setBlocked(User blocked) { this.blocked = blocked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}