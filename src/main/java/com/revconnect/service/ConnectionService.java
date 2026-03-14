package com.revconnect.service;

import com.revconnect.entity.Connection;
import com.revconnect.entity.User;
import com.revconnect.enums.ConnectionStatus;
import com.revconnect.enums.NotificationType;
import com.revconnect.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionService {

    private static final Logger logger = LogManager.getLogger(ConnectionService.class);

    private final ConnectionRepository connectionRepository;
    private final NotificationService notificationService;

    public Connection sendRequest(User requester, User receiver) {
        logger.info("Connection request from: {} to: {}", requester.getUsername(), receiver.getUsername());

        if (requester.equals(receiver)) {
            logger.warn("User: {} attempted to connect with themselves", requester.getUsername());
            throw new IllegalArgumentException("Cannot connect with yourself");
        }
        if (connectionRepository.hasPendingRequest(requester, receiver)) {
            logger.warn("Duplicate connection request from: {} to: {}", requester.getUsername(), receiver.getUsername());
            throw new IllegalArgumentException("Connection request already sent");
        }
        if (connectionRepository.areConnected(requester, receiver)) {
            logger.warn("Users already connected: {} and {}", requester.getUsername(), receiver.getUsername());
            throw new IllegalArgumentException("Already connected");
        }

        Connection connection = Connection.builder()
                .requester(requester)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();
        Connection saved = connectionRepository.save(connection);
        logger.info("Connection request saved with id: {} from: {} to: {}", saved.getId(), requester.getUsername(), receiver.getUsername());

        notificationService.createNotification(
                receiver, requester,
                NotificationType.CONNECTION_REQUEST,
                requester.getDisplayNameOrUsername() + " sent you a connection request",
                "/profile/" + requester.getUsername()
        );
        return saved;
    }

    public Connection acceptRequest(Long connectionId, User currentUser) {
        logger.info("User: {} accepting connection id: {}", currentUser.getUsername(), connectionId);

        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> {
                    logger.error("Connection not found with id: {}", connectionId);
                    return new IllegalArgumentException("Connection not found");
                });

        if (!conn.getReceiver().equals(currentUser)) {
            logger.warn("Unauthorized accept attempt on connection id: {} by user: {}", connectionId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        conn.setStatus(ConnectionStatus.ACCEPTED);
        Connection saved = connectionRepository.save(conn);
        logger.info("Connection id: {} accepted between: {} and: {}", connectionId, conn.getRequester().getUsername(), currentUser.getUsername());

        notificationService.createNotification(
                conn.getRequester(), currentUser,
                NotificationType.CONNECTION_ACCEPTED,
                currentUser.getDisplayNameOrUsername() + " accepted your connection request",
                "/profile/" + currentUser.getUsername()
        );
        return saved;
    }

    public void rejectRequest(Long connectionId, User currentUser) {
        logger.info("User: {} rejecting connection id: {}", currentUser.getUsername(), connectionId);

        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> {
                    logger.error("Connection not found with id: {}", connectionId);
                    return new IllegalArgumentException("Connection not found");
                });

        if (!conn.getReceiver().equals(currentUser)) {
            logger.warn("Unauthorized reject attempt on connection id: {} by user: {}", connectionId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        conn.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(conn);
        logger.info("Connection id: {} rejected by user: {}", connectionId, currentUser.getUsername());
    }

    public void removeConnection(User user, User other) {
        logger.info("Removing connection between: {} and: {}", user.getUsername(), other.getUsername());
        connectionRepository.findByRequesterAndReceiver(user, other)
                .ifPresent(c -> connectionRepository.delete(c));
        connectionRepository.findByRequesterAndReceiver(other, user)
                .ifPresent(c -> connectionRepository.delete(c));
        logger.info("Connection removed between: {} and: {}", user.getUsername(), other.getUsername());
    }

    @Transactional(readOnly = true)
    public List<User> getConnections(User user) {
        logger.debug("Fetching accepted connections for user: {}", user.getUsername());
        List<User> connections = connectionRepository.findAcceptedConnections(user).stream()
                .map(c -> c.getRequester().equals(user) ? c.getReceiver() : c.getRequester())
                .collect(Collectors.toList());
        logger.debug("User: {} has {} accepted connections", user.getUsername(), connections.size());
        return connections;
    }

    @Transactional(readOnly = true)
    public List<Connection> getPendingReceived(User user) {
        logger.debug("Fetching pending received requests for user: {}", user.getUsername());
        List<Connection> pending = connectionRepository.findPendingReceived(user);
        logger.debug("User: {} has {} pending received requests", user.getUsername(), pending.size());
        return pending;
    }

    @Transactional(readOnly = true)
    public List<Connection> getPendingSent(User user) {
        logger.debug("Fetching pending sent requests for user: {}", user.getUsername());
        List<Connection> pending = connectionRepository.findPendingSent(user);
        logger.debug("User: {} has {} pending sent requests", user.getUsername(), pending.size());
        return pending;
    }

    @Transactional(readOnly = true)
    public boolean areConnected(User u1, User u2) {
        boolean connected = connectionRepository.areConnected(u1, u2);
        logger.debug("Connection check between: {} and: {} -> {}", u1.getUsername(), u2.getUsername(), connected);
        return connected;
    }

    @Transactional(readOnly = true)
    public boolean hasPendingRequest(User requester, User receiver) {
        boolean pending = connectionRepository.hasPendingRequest(requester, receiver);
        logger.debug("Pending request check from: {} to: {} -> {}", requester.getUsername(), receiver.getUsername(), pending);
        return pending;
    }

    @Transactional(readOnly = true)
    public ConnectionStatus getStatus(User currentUser, User other) {
        logger.debug("Fetching connection status between: {} and: {}", currentUser.getUsername(), other.getUsername());
        return connectionRepository.findByRequesterAndReceiver(currentUser, other)
                .map(Connection::getStatus)
                .orElse(connectionRepository.findByRequesterAndReceiver(other, currentUser)
                        .map(Connection::getStatus)
                        .orElse(null));
    }
}