package com.spotcamp.module.authuser.controller;

import com.spotcamp.module.authuser.dto.*;
import com.spotcamp.module.authuser.service.AuthenticationService;
import com.spotcamp.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 * Implements the authentication endpoints defined in the OpenAPI specification
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication & Authorization")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
        summary = "User registration",
        description = "Create a new user account (Camper or Merchant)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request / Validation error"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletResponse response
    ) {
        log.info("Registration request for email: {}", request.getEmail());

        AuthResponseDTO authResponse = authenticationService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getRole(),
                request.getBusinessName()
        );

        setRefreshTokenCookie(response, authResponse.getAccessToken());
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @Operation(
        summary = "User login",
        description = "Authenticate user and receive JWT tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {
        log.info("Login request for email: {}", request.getEmail());

        AuthResponseDTO authResponse = authenticationService.login(
                request.getEmail(),
                request.getPassword()
        );

        setRefreshTokenCookie(response, authResponse.getAccessToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
        summary = "Refresh access token",
        description = "Get a new access token using refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        log.debug("Token refresh request");

        AuthResponseDTO authResponse = authenticationService.refreshToken(request.getRefreshToken());

        TokenResponseDTO response = TokenResponseDTO.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(request.getRefreshToken())
                .expiresIn(authResponse.getExpiresIn())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Logout user",
        description = "Invalidate tokens and clear session"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/logout")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<MessageResponseDTO> logout(
            Authentication authentication,
            HttpServletResponse response
    ) {
        if (authentication != null) {
            log.info("Logout request for user: {}", authentication.getName());
        }

        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(MessageResponseDTO.of("Logged out successfully"));
    }

    @Operation(
        summary = "Logout from all devices",
        description = "Invalidate all tokens for the user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logged out from all devices",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/logout-all")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<MessageResponseDTO> logoutAll(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response
    ) {
        if (principal != null) {
            log.info("Logout all request for user: {}", principal.getEmail());
            authenticationService.logoutAll(principal.getId());
        }

        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(MessageResponseDTO.of("Logged out from all devices"));
    }

    @Operation(
        summary = "Verify email address",
        description = "Verify user's email with token sent via email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponseDTO> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDTO request
    ) {
        log.debug("Email verification request");

        authenticationService.verifyEmail(request.getToken());
        return ResponseEntity.ok(MessageResponseDTO.of("Email verified successfully"));
    }

    @Operation(
        summary = "Resend verification email",
        description = "Resend email verification link"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verification email sent",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Email not found or already verified")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponseDTO> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDTO request
    ) {
        log.debug("Resend verification request for: {}", request.getEmail());

        authenticationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponseDTO.of("Verification email sent"));
    }

    @Operation(
        summary = "Request password reset",
        description = "Send password reset link to email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))
        )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(
            @Valid @RequestBody ResendVerificationRequestDTO request
    ) {
        log.debug("Forgot password request for: {}", request.getEmail());

        authenticationService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponseDTO.of("Password reset email sent"));
    }

    @Operation(
        summary = "Reset password with token",
        description = "Reset password using token from email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request
    ) {
        log.debug("Password reset request");

        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponseDTO.of("Password reset successfully"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", accessToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
}
