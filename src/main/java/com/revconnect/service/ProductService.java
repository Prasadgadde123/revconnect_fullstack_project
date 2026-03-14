package com.revconnect.service;

import com.revconnect.entity.Product;
import com.revconnect.entity.User;
import com.revconnect.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public List<Product> getProductsByOwner(User owner) {
        logger.debug("Fetching active products for user: {}", owner.getUsername());
        List<Product> products = productRepository.findByOwnerAndActiveTrueOrderByCreatedAtDesc(owner);
        logger.debug("Found {} active products for user: {}", products.size(), owner.getUsername());
        return products;
    }

    public Product createProduct(User owner, com.revconnect.dto.ProductCreateDTO dto) {
        logger.info("Creating product: '{}' for user: {}", dto.getName(), owner.getUsername());

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

        Product saved = productRepository.save(product);
        logger.info("Product created with id: {} name: '{}' by user: {}", saved.getId(), saved.getName(), owner.getUsername());
        return saved;
    }

    public Product updateProduct(Long id, User currentUser, com.revconnect.dto.ProductCreateDTO dto) {
        logger.info("Updating product id: {} by user: {}", id, currentUser.getUsername());

        Product p = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with id: {}", id);
                    return new IllegalArgumentException("Product not found");
                });

        if (!p.getOwner().equals(currentUser)) {
            logger.warn("Unauthorized update attempt on product id: {} by user: {}", id, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setCategory(dto.getCategory());
        p.setProductUrl(dto.getProductUrl());
        if (dto.getImageUrl() != null) {
            p.setImageUrl(dto.getImageUrl());
        }
        p.setActive(dto.isActive());

        Product updated = productRepository.save(p);
        logger.info("Product id: {} updated successfully by user: {}", id, currentUser.getUsername());
        return updated;
    }

    public void deleteProduct(Long id, User currentUser) {
        logger.info("Deleting product id: {} by user: {}", id, currentUser.getUsername());

        Product p = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with id: {}", id);
                    return new IllegalArgumentException("Product not found");
                });

        if (!p.getOwner().equals(currentUser)) {
            logger.warn("Unauthorized delete attempt on product id: {} by user: {}", id, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        p.setActive(false);
        productRepository.save(p);
        logger.info("Product id: {} deactivated (soft-deleted) by user: {}", id, currentUser.getUsername());
    }

    public Product getById(Long id) {
        logger.debug("Fetching product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with id: {}", id);
                    return new IllegalArgumentException("Product not found");
                });
    }
}