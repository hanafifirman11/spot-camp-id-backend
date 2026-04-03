package com.spotcamp.module.admin.service;

import com.spotcamp.module.admin.dto.AdminCampsiteListResponseDTO;
import com.spotcamp.module.admin.dto.AdminCampsiteSummaryResponseDTO;
import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.repository.UserRepository;
import com.spotcamp.module.campsite.entity.Campsite;
import com.spotcamp.module.campsite.entity.CampsiteStatus;
import com.spotcamp.module.campsite.repository.CampsiteRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCampsiteService {

    private final CampsiteRepository campsiteRepository;
    private final UserRepository userRepository;

    public AdminCampsiteListResponseDTO listCampsites(String query, CampsiteStatus status, Long businessId, int page, int size) {
        String trimmedQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Campsite> result = campsiteRepository.searchAdmin(trimmedQuery, status, businessId, pageable);

        Map<Long, User> owners = fetchOwners(result.getContent());
        List<AdminCampsiteSummaryResponseDTO> content = result.getContent().stream()
            .map(campsite -> toSummary(campsite, owners.get(campsite.getOwnerId())))
            .toList();

        return AdminCampsiteListResponseDTO.of(content, page, size, result.getTotalElements());
    }

    private Map<Long, User> fetchOwners(List<Campsite> campsites) {
        if (campsites == null || campsites.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> ownerIds = campsites.stream()
            .map(Campsite::getOwnerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (ownerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return StreamSupport.stream(userRepository.findAllById(ownerIds).spliterator(), false)
            .collect(Collectors.toMap(User::getId, user -> user));
    }

    private AdminCampsiteSummaryResponseDTO toSummary(Campsite campsite, User owner) {
        return AdminCampsiteSummaryResponseDTO.builder()
            .id(campsite.getId())
            .code(campsite.getCode())
            .name(campsite.getName())
            .location(campsite.getLocation())
            .address(campsite.getAddress())
            .status(campsite.getStatus())
            .minPrice(campsite.getMinPrice())
            .rating(campsite.getRating())
            .reviewCount(campsite.getReviewCount())
            .businessId(campsite.getOwnerId())
            .businessName(owner != null ? owner.getBusinessName() : null)
            .businessCode(owner != null ? owner.getBusinessCode() : null)
            .createdAt(campsite.getCreatedAt())
            .build();
    }
}
