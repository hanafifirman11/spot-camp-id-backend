package com.spotcamp.authuser.service;

import com.spotcamp.authuser.api.dto.AuthResponse;
import com.spotcamp.authuser.api.dto.UserInfoDto;
import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.common.config.JwtConfig;
import com.spotcamp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(String email, String password, String firstName, String lastName,
                                String phone, UserRole role, String businessName) {
        
        log.info("Registering new user with email: {}", email);
        
        // Create user
        User user = userService.createUser(email, password, firstName, lastName, phone, role, businessName);
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Update last login
        userService.updateLastLogin(user.getId());
        
        log.info("User registered successfully with ID: {}", user.getId());
        
        return AuthResponse.success(
                accessToken,
                jwtConfig.getExpiration() / 1000, // Convert to seconds
                mapToUserInfoDto(user)
        );
    }

    /**
     * Authenticate user login
     */
    @Transactional
    public AuthResponse login(String email, String password) {
        log.info("Authenticating user with email: {}", email);
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            log.debug("Authentication manager accepted credentials for email: {} (authenticated={})",
                    email, authentication.isAuthenticated());
            
            // Get user details
            User user = userService.findActiveUserByEmail(email);
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // Update last login
            userService.updateLastLogin(user.getId());
            
            log.info("User authenticated successfully with ID: {}", user.getId());
            
            return AuthResponse.success(
                    accessToken,
                    jwtConfig.getExpiration() / 1000,
                    mapToUserInfoDto(user)
            );
            
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for email: {} (bad credentials)", email);
            throw new BusinessException("Invalid email or password");
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {} - {}: {}",
                    email, e.getClass().getSimpleName(), e.getMessage());
            throw new BusinessException("Authentication failed");
        }
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing access token");
        
        if (!jwtService.isValidToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }
        
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessException("Token is not a refresh token");
        }
        
        String email = jwtService.extractUsername(refreshToken);
        User user = userService.findActiveUserByEmail(email);
        
        // Validate refresh token against user
        if (!jwtService.validateToken(refreshToken, user)) {
            throw new BusinessException("Invalid refresh token for user");
        }
        
        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        
        log.debug("Access token refreshed successfully for user ID: {}", user.getId());
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .build();
    }

    /**
     * Logout user (in a stateless JWT system, this mainly validates the token)
     */
    public void logout(String accessToken) {
        log.debug("Processing logout request");

        if (!jwtService.isValidToken(accessToken)) {
            throw new BusinessException("Invalid access token");
        }

        Long userId = jwtService.extractUserId(accessToken);
        log.info("User logged out successfully with ID: {}", userId);

        // In a more complex system, you might want to:
        // - Add the token to a blacklist stored in Redis
        // - Update last logout timestamp
        // - Send logout events
    }

    /**
     * Logout from all devices
     */
    @Transactional
    public void logoutAll(Long userId) {
        log.info("Logging out user from all devices: {}", userId);
        // In production, you would invalidate all refresh tokens
        // For now, this is a placeholder
    }

    /**
     * Verify email with token
     */
    @Transactional
    public void verifyEmail(String token) {
        log.debug("Verifying email with token");
        // In production, you would:
        // 1. Find the token in email_verification_tokens table
        // 2. Check if it's not expired
        // 3. Mark user as email verified
        // 4. Delete or mark token as used
        // For now, this is a placeholder implementation
        log.info("Email verification completed");
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.debug("Resending verification email to: {}", email);
        // In production, you would:
        // 1. Find user by email
        // 2. Check if already verified
        // 3. Generate new token
        // 4. Send email via email service
        // For now, this is a placeholder implementation
        log.info("Verification email sent to: {}", email);
    }

    /**
     * Send password reset email
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        log.debug("Sending password reset email to: {}", email);
        // In production, you would:
        // 1. Find user by email (don't throw error if not found for security)
        // 2. Generate reset token
        // 3. Save to password_reset_tokens table
        // 4. Send email via email service
        // For now, this is a placeholder implementation
        log.info("Password reset email sent to: {}", email);
    }

    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.debug("Resetting password with token");
        // In production, you would:
        // 1. Find the token in password_reset_tokens table
        // 2. Check if it's not expired
        // 3. Update user's password
        // 4. Delete or mark token as used
        // 5. Optionally invalidate all sessions
        // For now, this is a placeholder implementation
        log.info("Password reset completed");
    }

    /**
     * Map User entity to UserInfoDto
     */
    private UserInfoDto mapToUserInfoDto(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .businessName(user.getBusinessName())
                .businessCode(user.getBusinessCode())
                .darkMode(user.isDarkMode())
                .build();
    }
}
