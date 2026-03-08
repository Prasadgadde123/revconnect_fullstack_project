package com.revconnect.service;

import com.revconnect.entity.Product;
import com.revconnect.entity.User;
import com.revconnect.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getProductsByOwner(User owner) {
        return productRepository.findByOwnerAndActiveTrueOrderByCreatedAtDesc(owner);
    }

    public Product createProduct(User owner, com.revconnect.dto.ProductCreateDTO dto) {
        Product product = Product.builder()
                .owner(owner)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .productUrl(dto.getProductUrl())
                .imageUrl(dto.getImageUrl())
                .active(dto.isActive())
                .build();
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, User currentUser, com.revconnect.dto.ProductCreateDTO dto) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!p.getOwner().equals(currentUser)) throw new IllegalArgumentException("Unauthorized");
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setCategory(dto.getCategory());
        p.setProductUrl(dto.getProductUrl());
        if (dto.getImageUrl() != null) {
            p.setImageUrl(dto.getImageUrl());
        }
        p.setActive(dto.isActive());
        return productRepository.save(p);
    }

    public void deleteProduct(Long id, User currentUser) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!p.getOwner().equals(currentUser)) throw new IllegalArgumentException("Unauthorized");
        p.setActive(false);
        productRepository.save(p);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
}
