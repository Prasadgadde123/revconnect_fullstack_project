package com.revconnect;

import com.revconnect.dto.ProductCreateDTO;
import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.Product;
import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import com.revconnect.service.ProductService;
import com.revconnect.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired private ProductService productService;
    @Autowired private UserService userService;

    private User businessUser;

    @BeforeEach
    void setUp() {
        businessUser = userService.register(RegisterDTO.builder()
                .username("business_test").email("biz_test@test.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.BUSINESS).build());
    }

    @Test
    void testCreateProduct() {
        ProductCreateDTO dto = ProductCreateDTO.builder()
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();
        Product product = productService.createProduct(businessUser, dto);
        assertNotNull(product.getId());
        assertEquals("Test Product", product.getName());
    }

    @Test
    void testDeleteProduct() {
        Product product = productService.createProduct(businessUser, ProductCreateDTO.builder()
                .name("Delete Me").price(BigDecimal.ZERO).active(true).build());
        productService.deleteProduct(product.getId(), businessUser);
        Product deleted = productService.getById(product.getId());
        assertFalse(deleted.isActive());
    }
}