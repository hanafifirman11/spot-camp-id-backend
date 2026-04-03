package com.spotcamp.admin.service;

import com.spotcamp.admin.api.dto.AdminCampsiteListResponse;
import com.spotcamp.admin.api.dto.AdminCampsiteSummaryResponse;
import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.repository.UserRepository;
import com.spotcamp.campsite.domain.Campsite;
import com.spotcamp.campsite.domain.CampsiteStatus;
import com.spotcamp.campsite.repository.CampsiteRepository;
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

    public AdminCampsiteListResponse listCampsites(String query, CampsiteStatus status, Long businessId, int page, int size) {
        String trimmedQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Campsite> result = campsiteRepository.searchAdmin(trimmedQuery, status, businessId, pageable);

        Map<Long, User> owners = fetchOwners(result.getContent());
        List<AdminCampsiteSummaryResponse> content = result.getContent().stream()
            .map(campsite -> toSummary(campsite, owners.get(campsite.getOwnerId())))
            .toList();

        return AdminCampsiteListResponse.of(content, page, size, result.getTotalElements());
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

    private AdminCampsiteSummaryResponse toSummary(Campsite campsite, User owner) {
        return AdminCampsiteSummaryResponse.builder()
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
