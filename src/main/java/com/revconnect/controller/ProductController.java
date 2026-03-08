package com.revconnect.controller;

import com.revconnect.dto.ProductCreateDTO;
import com.revconnect.entity.Product;
import com.revconnect.entity.User;
import com.revconnect.service.NotificationService;
import com.revconnect.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final NotificationService notificationService;

    @GetMapping
    public String listMyProducts(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("products", productService.getProductsByOwner(currentUser));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "product/index";
    }

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser.getRole() == com.revconnect.enums.UserRole.PERSONAL || currentUser.getRole() == com.revconnect.enums.UserRole.ADMIN) {
            return "redirect:/explore";
        }
        model.addAttribute("productDTO", new ProductCreateDTO());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "product/create";
    }

    @PostMapping("/create")
    public String createProduct(@AuthenticationPrincipal User currentUser,
                                @Valid @ModelAttribute("productDTO") ProductCreateDTO dto,
                                BindingResult result,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                RedirectAttributes ra,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
            return "product/create";
        }

        if (file != null && !file.isEmpty()) {
            try {
                String uploadsDir = "uploads/products/";
                Files.createDirectories(Paths.get(uploadsDir));
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get(uploadsDir + filename), StandardCopyOption.REPLACE_EXISTING);
                dto.setImageUrl("/uploads/products/" + filename);
            } catch (IOException e) {
                ra.addFlashAttribute("error", "Failed to upload product image");
                return "redirect:/products/create";
            }
        }

        productService.createProduct(currentUser, dto);
        ra.addFlashAttribute("success", "Product added successfully!");
        return "redirect:/profile/" + currentUser.getUsername();
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, @AuthenticationPrincipal User currentUser, Model model) {
        Product p = productService.getById(id);
        if(!p.getOwner().equals(currentUser)) {
            return "redirect:/explore";
        }
        ProductCreateDTO dto = ProductCreateDTO.builder()
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .category(p.getCategory())
                .productUrl(p.getProductUrl())
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .build();
        model.addAttribute("productDTO", dto);
        model.addAttribute("product", p);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "product/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @AuthenticationPrincipal User currentUser,
                                @Valid @ModelAttribute("productDTO") ProductCreateDTO dto,
                                BindingResult result,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                RedirectAttributes ra,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
            return "product/edit";
        }

        if (file != null && !file.isEmpty()) {
            try {
                String uploadsDir = "uploads/products/";
                Files.createDirectories(Paths.get(uploadsDir));
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get(uploadsDir + filename), StandardCopyOption.REPLACE_EXISTING);
                dto.setImageUrl("/uploads/products/" + filename);
            } catch (IOException e) {
                ra.addFlashAttribute("error", "Failed to upload product image");
                return "redirect:/products/" + id + "/edit";
            }
        }

        productService.updateProduct(id, currentUser, dto);
        ra.addFlashAttribute("success", "Product updated!");
        return "redirect:/profile/" + currentUser.getUsername();
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, @AuthenticationPrincipal User currentUser, RedirectAttributes ra) {
        productService.deleteProduct(id, currentUser);
        ra.addFlashAttribute("success", "Product removed.");
        return "redirect:/profile/" + currentUser.getUsername();
    }
}
