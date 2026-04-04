package com.spotcamp.module.booking.controller;

import com.spotcamp.common.exception.ValidationException;
import com.spotcamp.module.booking.dto.BookingResponseDTO;
import com.spotcamp.module.booking.entity.Booking;
import com.spotcamp.module.booking.mapper.BookingMapper;
import com.spotcamp.module.booking.service.BookingService;
import com.spotcamp.module.booking.service.InvoicePdfService;
import com.spotcamp.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private InvoicePdfService invoicePdfService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingController, "paymentsUploadDir", "target/test-uploads");
    }

    @Test
    void uploadPaymentProof_ValidJpeg_ShouldSucceed() throws IOException {
        // Arrange
        Long userId = 1L;
        Long bookingId = 123L;
        when(authenticationFacade.getCurrentUserId()).thenReturn(userId);

        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "proof.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        Booking booking = new Booking();
        BookingResponseDTO responseDTO = org.mockito.Mockito.mock(BookingResponseDTO.class);
        when(bookingService.uploadPaymentProof(eq(userId), eq(bookingId), anyString())).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(responseDTO);

        // Act
        ResponseEntity<BookingResponseDTO> response = bookingController.uploadPaymentProof(bookingId, validFile);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void uploadPaymentProof_InvalidContentType_ShouldThrowValidationException() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 123L;
        when(authenticationFacade.getCurrentUserId()).thenReturn(userId);

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "script.sh",
                "application/x-sh",
                "echo 'hacked'".getBytes()
        );

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingController.uploadPaymentProof(bookingId, invalidFile);
        });

        assertTrue(exception.getMessage().contains("Only JPEG, PNG, and PDF files are allowed"));
    }

    @Test
    void uploadPaymentProof_OversizedFile_ShouldThrowValidationException() {
        // Arrange
        Long userId = 1L;
        Long bookingId = 123L;
        when(authenticationFacade.getCurrentUserId()).thenReturn(userId);

        MultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                new byte[0]
        ) {
            @Override
            public long getSize() {
                return 6L * 1024 * 1024; // 6 MB
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
            
            @Override
            public String getContentType() {
                return "application/pdf";
            }
        };

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            bookingController.uploadPaymentProof(bookingId, oversizedFile);
        });

        assertTrue(exception.getMessage().contains("File size must not exceed 5 MB"));
    }
}