package com.spotcamp.module.authuser.service;

import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.authuser.event.UserRegisteredEvent;
import com.spotcamp.module.authuser.repository.UserRepository;
import com.spotcamp.common.event.EventPublisher;
import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.common.exception.ValidationException;
import com.spotcamp.common.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Password validation pattern (min 8 chars, 1 uppercase, 1 lowercase, 1 digit)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    // Phone validation pattern (E.164 format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    /**
     * Create a new user account
     */
    @Transactional
    public User createUser(String email, String password, String firstName, String lastName,
                          String phone, UserRole role, String businessName) {
        
        log.info("Creating new user with email: {}", email);
        
        validateUserData(email, password, firstName, lastName, phone, role, businessName);
        
        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already registered");
        }

        String businessCode = null;
        if (role == UserRole.MERCHANT) {
            businessCode = CodeGenerator.generateUnique("BSN", 8, userRepository::existsByBusinessCode);
        }

        User user = User.builder()
                .email(email.toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phone(phone != null ? phone.trim() : null)
                .role(role)
                .businessName(businessName != null ? businessName.trim() : null)
                .businessCode(businessCode)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        // Publish user registration event
        eventPublisher.publishLocal(UserRegisteredEvent.create(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole(),
                savedUser.getBusinessName()
        ));
        
        return savedUser;
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    /**
     * Find user by ID
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Find active user by email (for authentication)
     */
    public User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active user", email));
    }

    /**
     * Update user profile
     */
    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName, 
                             String phone, String avatarUrl, Boolean darkMode) {
        
        User user = findById(userId);
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            validateName(firstName, "firstName");
            user.setFirstName(firstName.trim());
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            validateName(lastName, "lastName");
            user.setLastName(lastName.trim());
        }
        
        if (phone != null) {
            if (!phone.trim().isEmpty()) {
                validatePhone(phone);
                user.setPhone(phone.trim());
            } else {
                user.setPhone(null);
            }
        }
        
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl.trim().isEmpty() ? null : avatarUrl.trim());
        }

        if (darkMode != null) {
            user.setDarkMode(darkMode);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User profile updated for ID: {}", userId);
        
        return updatedUser;
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        // Validate new password
        validatePassword(newPassword);
        
        // Ensure new password is different
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException("New password must be different from current password");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Update last login timestamp
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    /**
     * Validate user data for registration
     */
    private void validateUserData(String email, String password, String firstName, 
                                 String lastName, String phone, UserRole role, String businessName) {
        
        validateEmail(email);
        validatePassword(password);
        validateName(firstName, "firstName");
        validateName(lastName, "lastName");
        
        if (phone != null && !phone.trim().isEmpty()) {
            validatePhone(phone);
        }
        
        if (role == UserRole.MERCHANT) {
            if (businessName == null || businessName.trim().isEmpty()) {
                throw new ValidationException("businessName", "Business name is required for merchants");
            }
            if (businessName.trim().length() > 200) {
                throw new ValidationException("businessName", "Business name cannot exceed 200 characters");
            }
        }
    }

    /**
     * Validate email format
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        
        if (email.length() > 255) {
            throw new ValidationException("email", "Email cannot exceed 255 characters");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email", "Invalid email format");
        }
    }

    /**
     * Validate password strength
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "Password is required");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("password", 
                "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                "one lowercase letter, and one number");
        }
    }

    /**
     * Validate name fields
     */
    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required");
        }
        
        if (name.trim().length() > 100) {
            throw new ValidationException(fieldName, fieldName + " cannot exceed 100 characters");
        }
    }

    /**
     * Validate phone number format
     */
    private void validatePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("phone", "Invalid phone number format (use E.164 format)");
        }
    }
}
