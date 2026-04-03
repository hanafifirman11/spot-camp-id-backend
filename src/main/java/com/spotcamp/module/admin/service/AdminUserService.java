package com.spotcamp.module.admin.service;

import com.spotcamp.module.admin.dto.AdminUserListResponseDTO;
import com.spotcamp.module.admin.dto.AdminUserSummaryResponseDTO;
import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.authuser.repository.UserRepository;
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

    public AdminUserListResponseDTO listUsers(String query, UserStatus status, UserRole role, int page, int size) {
        String trimmedQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.searchUsers(trimmedQuery, status, role, pageable);

        List<AdminUserSummaryResponseDTO> content = result.getContent().stream()
            .map(this::toSummary)
            .toList();

        return AdminUserListResponseDTO.of(content, page, size, result.getTotalElements());
    }

    private AdminUserSummaryResponseDTO toSummary(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String name = String.join(" ", firstName, lastName).trim();
        if (name.isEmpty()) {
            name = user.getEmail();
        }
        return AdminUserSummaryResponseDTO.builder()
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
