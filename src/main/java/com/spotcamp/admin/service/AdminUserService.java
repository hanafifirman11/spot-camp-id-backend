package com.spotcamp.admin.service;

import com.spotcamp.admin.api.dto.AdminUserListResponse;
import com.spotcamp.admin.api.dto.AdminUserSummaryResponse;
import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.authuser.domain.UserStatus;
import com.spotcamp.authuser.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserListResponse listUsers(String query, UserStatus status, UserRole role, int page, int size) {
        String trimmedQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.searchUsers(trimmedQuery, status, role, pageable);

        List<AdminUserSummaryResponse> content = result.getContent().stream()
            .map(this::toSummary)
            .toList();

        return AdminUserListResponse.of(content, page, size, result.getTotalElements());
    }

    private AdminUserSummaryResponse toSummary(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String name = String.join(" ", firstName, lastName).trim();
        if (name.isEmpty()) {
            name = user.getEmail();
        }
        return AdminUserSummaryResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(name)
            .role(user.getRole())
            .status(user.getStatus())
            .businessName(user.getBusinessName())
            .businessCode(user.getBusinessCode())
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
