package com.spotcamp.admin.service;

import com.spotcamp.admin.api.dto.AdminBusinessCampsiteListResponse;
import com.spotcamp.admin.api.dto.AdminBusinessCampsiteResponse;
import com.spotcamp.admin.api.dto.AdminBusinessDetailResponse;
import com.spotcamp.admin.api.dto.AdminBusinessListResponse;
import com.spotcamp.admin.api.dto.AdminBusinessSummaryResponse;
import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.authuser.domain.UserStatus;
import com.spotcamp.authuser.repository.UserRepository;
import com.spotcamp.campsite.domain.Campsite;
import com.spotcamp.campsite.domain.CampsiteStatus;
import com.spotcamp.campsite.repository.CampsiteRepository;
import com.spotcamp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBusinessService {

    private final UserRepository userRepository;
    private final CampsiteRepository campsiteRepository;

    public AdminBusinessListResponse listBusinesses(String query, UserStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> merchants = resolveMerchantPage(query, status, pageable);
        List<AdminBusinessSummaryResponse> content = merchants.getContent().stream()
            .map(this::mapSummary)
            .collect(Collectors.toList());
        return AdminBusinessListResponse.of(content, page, size, merchants.getTotalElements());
    }

    public AdminBusinessDetailResponse getBusiness(Long businessId) {
        User merchant = getMerchant(businessId);
        long totalCampsites = campsiteRepository.countByOwnerId(merchant.getId());
        long activeCampsites = campsiteRepository.countByOwnerIdAndStatus(merchant.getId(), CampsiteStatus.ACTIVE);
        return AdminBusinessDetailResponse.builder()
            .id(merchant.getId())
            .businessName(merchant.getBusinessName())
            .businessCode(merchant.getBusinessCode())
            .ownerName(merchant.getFullName())
            .email(merchant.getEmail())
            .phone(merchant.getPhone())
            .status(merchant.getStatus())
            .totalCampsites(totalCampsites)
            .activeCampsites(activeCampsites)
            .createdAt(merchant.getCreatedAt())
            .updatedAt(merchant.getUpdatedAt())
            .build();
    }

    public AdminBusinessCampsiteListResponse listBusinessCampsites(Long businessId, CampsiteStatus status, int page, int size) {
        getMerchant(businessId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Campsite> campsites;
        if (status != null) {
            campsites = campsiteRepository.findByOwnerIdAndStatus(businessId, status, pageable);
        } else {
            campsites = campsiteRepository.findByOwnerId(businessId, pageable);
        }
        List<AdminBusinessCampsiteResponse> content = campsites.getContent().stream()
            .map(this::mapCampsite)
            .collect(Collectors.toList());
        return AdminBusinessCampsiteListResponse.of(content, page, size, campsites.getTotalElements());
    }

    private Page<User> resolveMerchantPage(String query, UserStatus status, PageRequest pageable) {
        boolean hasQuery = StringUtils.hasText(query);
        List<UserRole> merchantRoles = List.of(UserRole.MERCHANT, UserRole.MERCHANT_ADMIN);
        if (hasQuery) {
            return userRepository.searchMerchants(query.trim(), status, pageable);
        }
        if (status != null) {
            return userRepository.findByRoleInAndStatus(merchantRoles, status, pageable);
        }
        return userRepository.findByRoleIn(merchantRoles, pageable);
    }

    private AdminBusinessSummaryResponse mapSummary(User user) {
        long totalCampsites = campsiteRepository.countByOwnerId(user.getId());
        long activeCampsites = campsiteRepository.countByOwnerIdAndStatus(user.getId(), CampsiteStatus.ACTIVE);
        return AdminBusinessSummaryResponse.builder()
            .id(user.getId())
            .businessName(user.getBusinessName())
            .businessCode(user.getBusinessCode())
            .ownerName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .status(user.getStatus())
            .totalCampsites(totalCampsites)
            .activeCampsites(activeCampsites)
            .createdAt(user.getCreatedAt())
            .build();
    }

    private User getMerchant(Long businessId) {
        return userRepository.findById(businessId)
            .filter(user -> user.getRole() == UserRole.MERCHANT || user.getRole() == UserRole.MERCHANT_ADMIN)
            .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
    }

    private AdminBusinessCampsiteResponse mapCampsite(Campsite campsite) {
        return AdminBusinessCampsiteResponse.builder()
            .id(campsite.getId())
            .code(campsite.getCode())
            .name(campsite.getName())
            .location(campsite.getLocation())
            .address(campsite.getAddress())
            .status(campsite.getStatus())
            .minPrice(campsite.getMinPrice())
            .rating(campsite.getRating())
            .reviewCount(campsite.getReviewCount())
            .createdAt(campsite.getCreatedAt())
            .build();
    }
}
