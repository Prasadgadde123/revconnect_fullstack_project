package com.revconnect.repository;

import com.revconnect.entity.Product;
import com.revconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByOwnerAndActiveTrueOrderByCreatedAtDesc(User owner);
}
