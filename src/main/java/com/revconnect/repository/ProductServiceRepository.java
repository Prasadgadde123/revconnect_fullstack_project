// src/main/java/com/revconnect/repository/ProductServiceRepository.java
package com.revconnect.repository;

import com.revconnect.model.user.ProductService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductServiceRepository extends JpaRepository<ProductService, Long> {
    List<ProductService> findByBusinessProfileIdAndStatusOrderByDisplayOrderAsc(Long businessProfileId, String status);
    List<ProductService> findByBusinessProfileIdOrderByDisplayOrderAsc(Long businessProfileId);
    void deleteByBusinessProfileId(Long businessProfileId);
}