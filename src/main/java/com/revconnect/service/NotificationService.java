package com.revconnect.service;

import com.revconnect.entity.Notification;
import com.revconnect.entity.User;
import com.revconnect.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(User recipient, User actor, NotificationType type, String message, String linkUrl) {
        if (recipient.equals(actor)) return; // Don't notify yourself

        // Check user preferences
        boolean shouldNotify = switch (type) {
            case CONNECTION_REQUEST, CONNECTION_ACCEPTED -> recipient.isNotifyConnectionRequests();
            case NEW_FOLLOWER -> recipient.isNotifyFollowers();
            case POST_LIKED -> recipient.isNotifyLikes();
            case POST_COMMENTED -> recipient.isNotifyComments();
            case POST_SHARED -> recipient.isNotifyShares();
            default -> true;
        };

        if (!shouldNotify) return;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .message(message)
                .linkUrl(linkUrl)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification created for {}: {}", recipient.getUsername(), message);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().equals(user)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
}
