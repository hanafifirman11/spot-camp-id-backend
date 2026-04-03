package com.spotcamp.module.admin.service;

import com.spotcamp.module.admin.dto.AdminBusinessCampsiteListResponseDTO;
import com.spotcamp.module.admin.dto.AdminBusinessCampsiteResponseDTO;
import com.spotcamp.module.admin.dto.AdminBusinessDetailResponseDTO;
import com.spotcamp.module.admin.dto.AdminBusinessListResponseDTO;
import com.spotcamp.module.admin.dto.AdminBusinessSummaryResponseDTO;
import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.authuser.repository.UserRepository;
import com.spotcamp.module.campsite.entity.Campsite;
import com.spotcamp.module.campsite.entity.CampsiteStatus;
import com.spotcamp.module.campsite.repository.CampsiteRepository;
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

    public AdminBusinessListResponseDTO listBusinesses(String query, UserStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> merchants = resolveMerchantPage(query, status, pageable);
        List<AdminBusinessSummaryResponseDTO> content = merchants.getContent().stream()
            .map(this::mapSummary)
            .collect(Collectors.toList());
        return AdminBusinessListResponseDTO.of(content, page, size, merchants.getTotalElements());
    }

    public AdminBusinessDetailResponseDTO getBusiness(Long businessId) {
        User merchant = getMerchant(businessId);
        long totalCampsites = campsiteRepository.countByOwnerId(merchant.getId());
        long activeCampsites = campsiteRepository.countByOwnerIdAndStatus(merchant.getId(), CampsiteStatus.ACTIVE);
        return AdminBusinessDetailResponseDTO.builder()
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

    public AdminBusinessCampsiteListResponseDTO listBusinessCampsites(Long businessId, CampsiteStatus status, int page, int size) {
        getMerchant(businessId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Campsite> campsites;
        if (status != null) {
            campsites = campsiteRepository.findByOwnerIdAndStatus(businessId, status, pageable);
        } else {
            campsites = campsiteRepository.findByOwnerId(businessId, pageable);
        }
        List<AdminBusinessCampsiteResponseDTO> content = campsites.getContent().stream()
            .map(this::mapCampsite)
            .collect(Collectors.toList());
        return AdminBusinessCampsiteListResponseDTO.of(content, page, size, campsites.getTotalElements());
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

    private AdminBusinessSummaryResponseDTO mapSummary(User user) {
        long totalCampsites = campsiteRepository.countByOwnerId(user.getId());
        long activeCampsites = campsiteRepository.countByOwnerIdAndStatus(user.getId(), CampsiteStatus.ACTIVE);
        return AdminBusinessSummaryResponseDTO.builder()
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

    private AdminBusinessCampsiteResponseDTO mapCampsite(Campsite campsite) {
        return AdminBusinessCampsiteResponseDTO.builder()
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
