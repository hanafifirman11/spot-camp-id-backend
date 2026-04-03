package com.spotcamp.authuser.service;

import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.authuser.domain.UserStatus;
import com.spotcamp.authuser.repository.UserRepository;
import com.spotcamp.common.event.EventPublisher;
import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("+6281234567890")
                .role(UserRole.CAMPER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
    }

    @Test
    void createUser_ValidCamper_Success() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(
                "test@example.com",
                "Password123!",
                "John",
                "Doe",
                "+6281234567890",
                UserRole.CAMPER,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(UserRole.CAMPER, result.getRole());
        
        verify(userRepository).existsByEmailIgnoreCase("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishLocal(any());
    }

    @Test
    void createUser_ValidMerchant_Success() {
        // Arrange
        User merchantUser = User.builder()
                .id(2L)
                .email("merchant@example.com")
                .passwordHash("encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .role(UserRole.MERCHANT)
                .businessName("Test Business")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(merchantUser);

        // Act
        User result = userService.createUser(
                "merchant@example.com",
                "Password123!",
                "Jane",
                "Smith",
                null,
                UserRole.MERCHANT,
                "Test Business"
        );

        // Assert
        assertNotNull(result);
        assertEquals("merchant@example.com", result.getEmail());
        assertEquals(UserRole.MERCHANT, result.getRole());
        assertEquals("Test Business", result.getBusinessName());
        
        verify(eventPublisher).publishLocal(any());
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(
                        "test@example.com",
                        "Password123!",
                        "John",
                        "Doe",
                        null,
                        UserRole.CAMPER,
                        null
                )
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishLocal(any());
    }

    @Test
    void createUser_InvalidEmail_ThrowsException() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(
                        "invalid-email",
                        "Password123!",
                        "John",
                        "Doe",
                        null,
                        UserRole.CAMPER,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void createUser_WeakPassword_ThrowsException() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(
                        "test@example.com",
                        "weak",
                        "John",
                        "Doe",
                        null,
                        UserRole.CAMPER,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
    }

    @Test
    void createUser_MerchantWithoutBusinessName_ThrowsException() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(
                        "test@example.com",
                        "Password123!",
                        "John",
                        "Doe",
                        null,
                        UserRole.MERCHANT,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("Business name is required"));
    }

    @Test
    void updateProfile_ValidData_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateProfile(1L, "Jane", "Smith", "+6289876543210", "avatar.jpg", true);

        // Assert
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("+6289876543210", result.getPhone());
        assertEquals("avatar.jpg", result.getAvatarUrl());
        
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_ValidPassword_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123!", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> 
                userService.changePassword(1L, "currentPassword", "NewPassword123!")
        );

        // Assert
        verify(passwordEncoder).encode("NewPassword123!");
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.changePassword(1L, "wrongPassword", "NewPassword123!")
        );

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
