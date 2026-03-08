package com.revconnect.service;

import com.revconnect.entity.Connection;
import com.revconnect.entity.User;
import com.revconnect.enums.ConnectionStatus;
import com.revconnect.enums.NotificationType;
import com.revconnect.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final NotificationService notificationService;

    public Connection sendRequest(User requester, User receiver) {
        if (requester.equals(receiver)) throw new IllegalArgumentException("Cannot connect with yourself");

        if (connectionRepository.hasPendingRequest(requester, receiver)) {
            throw new IllegalArgumentException("Connection request already sent");
        }
        if (connectionRepository.areConnected(requester, receiver)) {
            throw new IllegalArgumentException("Already connected");
        }

        Connection connection = Connection.builder()
                .requester(requester)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();
        Connection saved = connectionRepository.save(connection);

        notificationService.createNotification(
                receiver, requester,
                NotificationType.CONNECTION_REQUEST,
                requester.getDisplayNameOrUsername() + " sent you a connection request",
                "/profile/" + requester.getUsername()
        );
        return saved;
    }

    public Connection acceptRequest(Long connectionId, User currentUser) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        if (!conn.getReceiver().equals(currentUser)) throw new IllegalArgumentException("Unauthorized");
        conn.setStatus(ConnectionStatus.ACCEPTED);
        Connection saved = connectionRepository.save(conn);

        notificationService.createNotification(
                conn.getRequester(), currentUser,
                NotificationType.CONNECTION_ACCEPTED,
                currentUser.getDisplayNameOrUsername() + " accepted your connection request",
                "/profile/" + currentUser.getUsername()
        );
        return saved;
    }

    public void rejectRequest(Long connectionId, User currentUser) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        if (!conn.getReceiver().equals(currentUser)) throw new IllegalArgumentException("Unauthorized");
        conn.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(conn);
    }

    public void removeConnection(User user, User other) {
        connectionRepository.findByRequesterAndReceiver(user, other)
                .ifPresent(c -> connectionRepository.delete(c));
        connectionRepository.findByRequesterAndReceiver(other, user)
                .ifPresent(c -> connectionRepository.delete(c));
    }

    @Transactional(readOnly = true)
    public List<User> getConnections(User user) {
        return connectionRepository.findAcceptedConnections(user).stream()
                .map(c -> c.getRequester().equals(user) ? c.getReceiver() : c.getRequester())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Connection> getPendingReceived(User user) {
        return connectionRepository.findPendingReceived(user);
    }

    @Transactional(readOnly = true)
    public List<Connection> getPendingSent(User user) {
        return connectionRepository.findPendingSent(user);
    }

    @Transactional(readOnly = true)
    public boolean areConnected(User u1, User u2) {
        return connectionRepository.areConnected(u1, u2);
    }

    @Transactional(readOnly = true)
    public boolean hasPendingRequest(User requester, User receiver) {
        return connectionRepository.hasPendingRequest(requester, receiver);
    }

    @Transactional(readOnly = true)
    public ConnectionStatus getStatus(User currentUser, User other) {
        return connectionRepository.findByRequesterAndReceiver(currentUser, other)
                .map(Connection::getStatus)
                .orElse(connectionRepository.findByRequesterAndReceiver(other, currentUser)
                        .map(Connection::getStatus)
                        .orElse(null));
    }
}
