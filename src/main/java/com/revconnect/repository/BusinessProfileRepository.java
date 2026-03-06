// src/main/java/com/revconnect/repository/BusinessProfileRepository.java
package com.revconnect.repository;

import com.revconnect.model.user.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    Optional<BusinessProfile> findByUserId(Long userId);
    Optional<BusinessProfile> findByUserUsername(String username);
    boolean existsByUserId(Long userId);
}