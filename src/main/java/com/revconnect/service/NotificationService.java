package com.revconnect.service;

import com.revconnect.entity.Notification;
import com.revconnect.entity.User;
import com.revconnect.enums.NotificationType;
import com.revconnect.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public void createNotification(User recipient, User actor, NotificationType type, String message, String linkUrl) {
        if (recipient.equals(actor)) {
            logger.debug("Skipping self-notification for user: {}", recipient.getUsername());
            return;
        }

        boolean shouldNotify = switch (type) {
            case CONNECTION_REQUEST, CONNECTION_ACCEPTED -> recipient.isNotifyConnectionRequests();
            case NEW_FOLLOWER -> recipient.isNotifyFollowers();
            case POST_LIKED -> recipient.isNotifyLikes();
            case POST_COMMENTED -> recipient.isNotifyComments();
            case POST_SHARED -> recipient.isNotifyShares();
            default -> true;
        };

        if (!shouldNotify) {
            logger.debug("Notification suppressed for user: {} - type: {} is disabled in preferences", recipient.getUsername(), type);
            return;
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .message(message)
                .linkUrl(linkUrl)
                .build();

        notificationRepository.save(notification);
        logger.debug("Notification created for user: {} - type: {}, message: {}", recipient.getUsername(), type, message);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(User user) {
        logger.debug("Fetching notifications for user: {}", user.getUsername());
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        logger.debug("User: {} has {} notifications", user.getUsername(), notifications.size());
        return notifications;
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        long count = notificationRepository.countByRecipientAndReadFalse(user);
        logger.debug("User: {} has {} unread notifications", user.getUsername(), count);
        return count;
    }

    public void markAsRead(Long notificationId, User user) {
        logger.debug("Marking notification id: {} as read for user: {}", notificationId, user.getUsername());
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().equals(user)) {
                n.setRead(true);
                notificationRepository.save(n);
                logger.debug("Notification id: {} marked as read", notificationId);
            } else {
                logger.warn("User: {} attempted to mark notification id: {} owned by another user", user.getUsername(), notificationId);
            }
        });
    }

    public void markAllAsRead(User user) {
        logger.info("Marking all notifications as read for user: {}", user.getUsername());
        notificationRepository.markAllAsRead(user);
        logger.info("All notifications marked as read for user: {}", user.getUsername());
    }
}