package com.revconnect.repository;

import com.revconnect.entity.Connection;
import com.revconnect.entity.User;
import com.revconnect.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findByRequesterAndReceiver(User requester, User receiver);

    @Query("SELECT c FROM Connection c WHERE (c.requester = :u OR c.receiver = :u) AND c.status = 'ACCEPTED'")
    List<Connection> findAcceptedConnections(@Param("u") User user);

    @Query("SELECT c FROM Connection c WHERE c.receiver = :u AND c.status = 'PENDING'")
    List<Connection> findPendingReceived(@Param("u") User user);

    @Query("SELECT c FROM Connection c WHERE c.requester = :u AND c.status = 'PENDING'")
    List<Connection> findPendingSent(@Param("u") User user);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Connection c " +
           "WHERE ((c.requester = :u1 AND c.receiver = :u2) OR (c.requester = :u2 AND c.receiver = :u1)) " +
           "AND c.status = 'ACCEPTED'")
    boolean areConnected(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Connection c " +
           "WHERE c.requester = :requester AND c.receiver = :receiver AND c.status = 'PENDING'")
    boolean hasPendingRequest(@Param("requester") User requester, @Param("receiver") User receiver);
}
