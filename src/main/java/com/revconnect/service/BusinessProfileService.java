// src/main/java/com/revconnect/service/BusinessProfileService.java
package com.revconnect.service;

import com.revconnect.dto.request.BusinessProfileRequest;
import com.revconnect.dto.request.ExternalLinkRequest;
import com.revconnect.dto.request.ProductServiceRequest;
import com.revconnect.dto.response.BusinessProfileResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.model.user.BusinessProfile;
import com.revconnect.model.user.ExternalLink;
import com.revconnect.model.user.ProductService;
import com.revconnect.model.user.User;
import com.revconnect.repository.BusinessProfileRepository;
import com.revconnect.repository.ExternalLinkRepository;
import com.revconnect.repository.ProductServiceRepository;
import com.revconnect.repository.UserRepository;
import com.revconnect.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final ProductServiceRepository productServiceRepository;
    private final UserRepository userRepository;

    public BusinessProfileService(BusinessProfileRepository businessProfileRepository,
                                  ExternalLinkRepository externalLinkRepository,
                                  ProductServiceRepository productServiceRepository,
                                  UserRepository userRepository) {
        this.businessProfileRepository = businessProfileRepository;
        this.externalLinkRepository = externalLinkRepository;
        this.productServiceRepository = productServiceRepository;
        this.userRepository = userRepository;
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl details = (UserDetailsImpl) auth.getPrincipal();
        return userRepository.findById(details.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private void assertBusinessOrCreator(User user) {
        String type = user.getUserType();
        if (!"BUSINESS".equalsIgnoreCase(type) && !"CREATOR".equalsIgnoreCase(type)) {
            throw new RuntimeException("Only BUSINESS or CREATOR accounts can manage a business profile");
        }
    }

    private BusinessProfile getOrCreateProfile(User user) {
        return businessProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> businessProfileRepository.save(new BusinessProfile(user)));
    }

    // ── Profile CRUD ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public BusinessProfileResponse getMyBusinessProfile() {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        BusinessProfile bp = businessProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business profile not set up yet"));
        return BusinessProfileResponse.from(bp);
    }

    @Transactional(readOnly = true)
    public BusinessProfileResponse getBusinessProfileByUsername(String username) {
        BusinessProfile bp = businessProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Business profile not found for: " + username));
        return BusinessProfileResponse.from(bp);
    }

    public MessageResponse upsertBusinessProfile(BusinessProfileRequest request) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        BusinessProfile bp = getOrCreateProfile(user);

        if (request.getBusinessName() != null) bp.setBusinessName(request.getBusinessName());
        if (request.getCategory()     != null) bp.setCategory(request.getCategory());
        if (request.getIndustry()     != null) bp.setIndustry(request.getIndustry());
        if (request.getDetailedBio()  != null) bp.setDetailedBio(request.getDetailedBio());
        if (request.getContactEmail() != null) bp.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) bp.setContactPhone(request.getContactPhone());
        if (request.getWebsiteUrl()   != null) bp.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getFacebookUrl()  != null) bp.setFacebookUrl(request.getFacebookUrl());
        if (request.getTwitterUrl()   != null) bp.setTwitterUrl(request.getTwitterUrl());
        if (request.getInstagramUrl() != null) bp.setInstagramUrl(request.getInstagramUrl());
        if (request.getLinkedinUrl()  != null) bp.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getYoutubeUrl()   != null) bp.setYoutubeUrl(request.getYoutubeUrl());
        if (request.getTiktokUrl()    != null) bp.setTiktokUrl(request.getTiktokUrl());

        // Address & hours — BUSINESS accounts only
        if ("BUSINESS".equalsIgnoreCase(user.getUserType())) {
            if (request.getStreetAddress() != null) bp.setStreetAddress(request.getStreetAddress());
            if (request.getCity()          != null) bp.setCity(request.getCity());
            if (request.getStateProvince() != null) bp.setStateProvince(request.getStateProvince());
            if (request.getPostalCode()    != null) bp.setPostalCode(request.getPostalCode());
            if (request.getCountry()       != null) bp.setCountry(request.getCountry());
            if (request.getBusinessHours() != null) bp.setBusinessHours(request.getBusinessHours());
            if (request.getTimezone()      != null) bp.setTimezone(request.getTimezone());
        }

        businessProfileRepository.save(bp);
        return new MessageResponse("Business profile updated successfully");
    }

    // ── External Links ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ExternalLink> getExternalLinks() {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        BusinessProfile bp = businessProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business profile not found"));
        return externalLinkRepository.findByBusinessProfileIdOrderByDisplayOrderAsc(bp.getId());
    }

    public MessageResponse addExternalLink(ExternalLinkRequest request) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        BusinessProfile bp = getOrCreateProfile(user);

        ExternalLink link = new ExternalLink(bp, request.getLabel(), request.getUrl(), request.getLinkType());
        link.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        externalLinkRepository.save(link);
        return new MessageResponse("External link added successfully");
    }

    public MessageResponse updateExternalLink(Long linkId, ExternalLinkRequest request) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        ExternalLink link = externalLinkRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Link not found"));
        if (!link.getBusinessProfile().getUser().getId().equals(user.getId()))
            throw new RuntimeException("Not authorized to update this link");
        if (request.getLabel()        != null) link.setLabel(request.getLabel());
        if (request.getUrl()          != null) link.setUrl(request.getUrl());
        if (request.getLinkType()     != null) link.setLinkType(request.getLinkType());
        if (request.getDisplayOrder() != null) link.setDisplayOrder(request.getDisplayOrder());
        externalLinkRepository.save(link);
        return new MessageResponse("External link updated successfully");
    }

    public MessageResponse deleteExternalLink(Long linkId) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        ExternalLink link = externalLinkRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Link not found"));
        if (!link.getBusinessProfile().getUser().getId().equals(user.getId()))
            throw new RuntimeException("Not authorized to delete this link");
        externalLinkRepository.delete(link);
        return new MessageResponse("External link deleted successfully");
    }

    // ── Products & Services ───────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductService> getProductsServices(String username) {
        BusinessProfile bp = businessProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Business profile not found for: " + username));
        return productServiceRepository
                .findByBusinessProfileIdAndStatusOrderByDisplayOrderAsc(bp.getId(), "ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<ProductService> getMyProductsServices() {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        BusinessProfile bp = businessProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business profile not found"));
        return productServiceRepository.findByBusinessProfileIdOrderByDisplayOrderAsc(bp.getId());
    }

    public MessageResponse addProductService(ProductServiceRequest request) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        BusinessProfile bp = getOrCreateProfile(user);

        ProductService ps = new ProductService(bp, request.getName(), request.getItemType());
        ps.setDescription(request.getDescription());
        ps.setPrice(request.getPrice());
        ps.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        ps.setImageUrl(request.getImageUrl());
        ps.setPurchaseUrl(request.getPurchaseUrl());
        ps.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        ps.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        productServiceRepository.save(ps);
        return new MessageResponse("Product/Service added successfully");
    }

    public MessageResponse updateProductService(Long itemId, ProductServiceRequest request) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        ProductService ps = productServiceRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Product/Service not found"));
        if (!ps.getBusinessProfile().getUser().getId().equals(user.getId()))
            throw new RuntimeException("Not authorized to update this item");
        if (request.getName()         != null) ps.setName(request.getName());
        if (request.getDescription()  != null) ps.setDescription(request.getDescription());
        if (request.getItemType()     != null) ps.setItemType(request.getItemType());
        if (request.getPrice()        != null) ps.setPrice(request.getPrice());
        if (request.getCurrency()     != null) ps.setCurrency(request.getCurrency());
        if (request.getImageUrl()     != null) ps.setImageUrl(request.getImageUrl());
        if (request.getPurchaseUrl()  != null) ps.setPurchaseUrl(request.getPurchaseUrl());
        if (request.getStatus()       != null) ps.setStatus(request.getStatus());
        if (request.getDisplayOrder() != null) ps.setDisplayOrder(request.getDisplayOrder());
        productServiceRepository.save(ps);
        return new MessageResponse("Product/Service updated successfully");
    }

    public MessageResponse deleteProductService(Long itemId) {
        User user = getCurrentUser();
        assertBusinessOrCreator(user);
        ProductService ps = productServiceRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Product/Service not found"));
        if (!ps.getBusinessProfile().getUser().getId().equals(user.getId()))
            throw new RuntimeException("Not authorized to delete this item");
        productServiceRepository.delete(ps);
        return new MessageResponse("Product/Service deleted successfully");
    }
}