// src/main/java/com/revconnect/controller/api/BusinessProfileController.java
package com.revconnect.controller.api;

import com.revconnect.dto.request.BusinessProfileRequest;
import com.revconnect.dto.request.ExternalLinkRequest;
import com.revconnect.dto.request.ProductServiceRequest;
import com.revconnect.dto.response.BusinessProfileResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.model.user.ExternalLink;
import com.revconnect.model.user.ProductService;
import com.revconnect.service.BusinessProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/business-profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    public BusinessProfileController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    // ── Profile ──────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<?> getMyBusinessProfile() {
        try {
            return ResponseEntity.ok(businessProfileService.getMyBusinessProfile());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getBusinessProfileByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(businessProfileService.getBusinessProfileByUsername(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> upsertBusinessProfile(@Valid @RequestBody BusinessProfileRequest request) {
        try {
            return ResponseEntity.ok(businessProfileService.upsertBusinessProfile(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ── External Links ────────────────────────────────────────

    @GetMapping("/links")
    public ResponseEntity<?> getExternalLinks() {
        try {
            List<ExternalLink> links = businessProfileService.getExternalLinks();
            return ResponseEntity.ok(links);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/links")
    public ResponseEntity<?> addExternalLink(@Valid @RequestBody ExternalLinkRequest request) {
        try {
            return ResponseEntity.ok(businessProfileService.addExternalLink(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/links/{linkId}")
    public ResponseEntity<?> updateExternalLink(@PathVariable Long linkId,
                                                @RequestBody ExternalLinkRequest request) {
        try {
            return ResponseEntity.ok(businessProfileService.updateExternalLink(linkId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/links/{linkId}")
    public ResponseEntity<?> deleteExternalLink(@PathVariable Long linkId) {
        try {
            return ResponseEntity.ok(businessProfileService.deleteExternalLink(linkId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ── Products / Services ───────────────────────────────────

    /** Public — returns only ACTIVE items */
    @GetMapping("/{username}/products")
    public ResponseEntity<?> getProductsServices(@PathVariable String username) {
        try {
            List<ProductService> items = businessProfileService.getProductsServices(username);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Owner — returns all items regardless of status */
    @GetMapping("/products/me")
    public ResponseEntity<?> getMyProductsServices() {
        try {
            return ResponseEntity.ok(businessProfileService.getMyProductsServices());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProductService(@Valid @RequestBody ProductServiceRequest request) {
        try {
            return ResponseEntity.ok(businessProfileService.addProductService(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/products/{itemId}")
    public ResponseEntity<?> updateProductService(@PathVariable Long itemId,
                                                  @RequestBody ProductServiceRequest request) {
        try {
            return ResponseEntity.ok(businessProfileService.updateProductService(itemId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/products/{itemId}")
    public ResponseEntity<?> deleteProductService(@PathVariable Long itemId) {
        try {
            return ResponseEntity.ok(businessProfileService.deleteProductService(itemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}