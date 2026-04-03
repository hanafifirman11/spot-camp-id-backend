package com.spotcamp.module.authuser.service;

import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.authuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getMerchantUsers(String businessCode) {
        return userRepository.findByBusinessCode(businessCode);
    }

    @Transactional
    public User createMerchantUser(String businessCode, String email, String firstName, String lastName, UserRole role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Validate role scope
        if (role != UserRole.MERCHANT_ADMIN && role != UserRole.MERCHANT_MEMBER) {
            throw new IllegalArgumentException("Invalid role for merchant user");
        }

        // Inherit business name from an existing user of this code if possible
        // Or leave it null, assuming businessCode is the link. 
        // Ideally we fetch the "Business" entity, but here we scan for a peer.
        String businessName = userRepository.findByBusinessCode(businessCode).stream()
                .findFirst()
                .map(User::getBusinessName)
                .orElse("Merchant Team");

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password, assume invite flow
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .businessCode(businessCode)
                .businessName(businessName)
                .status(UserStatus.ACTIVE) // Or PENDING if invite flow
                .emailVerified(true) // Simplify for now
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long userId, String businessCode, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getBusinessCode().equals(businessCode)) {
            throw new RuntimeException("User does not belong to your merchant account");
        }

        if (newRole != UserRole.MERCHANT_ADMIN && newRole != UserRole.MERCHANT_MEMBER) {
            throw new IllegalArgumentException("Invalid role");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserStatus(Long userId, String businessCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getBusinessCode().equals(businessCode)) {
            throw new RuntimeException("User does not belong to your merchant account");
        }

        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
