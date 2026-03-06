// src/main/java/com/revconnect/repository/ExternalLinkRepository.java
package com.revconnect.repository;

import com.revconnect.model.user.ExternalLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalLinkRepository extends JpaRepository<ExternalLink, Long> {
    List<ExternalLink> findByBusinessProfileIdOrderByDisplayOrderAsc(Long businessProfileId);
    void deleteByBusinessProfileId(Long businessProfileId);
}