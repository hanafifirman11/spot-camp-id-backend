package com.spotcamp.module.booking.controller;

import com.spotcamp.module.booking.entity.Booking;
import com.spotcamp.module.booking.entity.BookingStatus;
import com.spotcamp.module.booking.dto.BookingRequestDTO;
import com.spotcamp.module.booking.dto.BookingResponseDTO;
import com.spotcamp.module.booking.dto.CheckoutRequestDTO;
import com.spotcamp.module.booking.dto.VerifyPaymentRequestDTO;
import com.spotcamp.module.booking.mapper.BookingMapper;
import com.spotcamp.module.booking.service.BookingService;
import com.spotcamp.module.booking.service.InvoicePdfService;
import com.spotcamp.security.AuthenticationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

import com.spotcamp.common.exception.ValidationException;
import com.spotcamp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API for booking operations
 */
@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking and cart management operations")
public class BookingController {

    @Value("${app.upload.base-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE,
        "application/pdf"
    );
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final AuthenticationFacade authenticationFacade;
    private final InvoicePdfService invoicePdfService;

    @GetMapping("/cart")
    @Operation(summary = "Get current user's cart", description = "Retrieves the current user's shopping cart")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> getCart() {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking cart = bookingService.getOrCreateCart(userId);
        return ResponseEntity.ok(bookingMapper.toResponse(cart));
    }

    @PostMapping("/cart/items")
    @Operation(summary = "Add item to cart", description = "Adds a camping spot or product to the user's cart")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> addToCart(@Valid @RequestBody BookingRequestDTO request) {
        Long userId = authenticationFacade.getCurrentUserId();
        
        Booking updatedCart = bookingService.addItemToCart(
                userId, 
                request.getProductId(),
                request.getSpotId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getQuantity()
        );
        
        return ResponseEntity.ok(bookingMapper.toResponse(updatedCart));
    }

    @DeleteMapping("/cart/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the user's cart")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> removeFromCart(@PathVariable Long itemId) {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking updatedCart = bookingService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(bookingMapper.toResponse(updatedCart));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Proceed to checkout", description = "Moves cart to payment pending status")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> checkout(@Valid @RequestBody CheckoutRequestDTO request) {
        Long userId = authenticationFacade.getCurrentUserId();
        
        Booking booking = bookingService.proceedToCheckout(
                userId,
                request.getContactName(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getSpecialRequests(),
                request.getPaymentMethod(),
                request.getPaymentChannel()
        );
        
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PostMapping(value = "/{bookingId}/payment-proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload payment proof", description = "Uploads transfer proof for manual payment")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> uploadPaymentProof(
            @PathVariable Long bookingId,
            @RequestParam("file") MultipartFile file) {
        Long userId = authenticationFacade.getCurrentUserId();

        // 1. Presence check
        if (file == null || file.isEmpty()) {
            throw new ValidationException("file", "File is required and cannot be empty");
        }

        // 2. Content-type check
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ValidationException("file", "Only JPEG, PNG, and PDF files are allowed");
        }

        // 3. Size check
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ValidationException("file", "File size must not exceed 5 MB");
        }

        // 4. Build safe filename — never use original filename
        String extension = switch (contentType) {
            case "image/jpeg"    -> ".jpg";
            case "image/png"     -> ".png";
            case "application/pdf" -> ".pdf";
            default -> throw new ValidationException("file", "Unsupported file type"); // safety net, should not reach here
        };
        String safeFileName = UUID.randomUUID() + extension;

        // 5. Save
        try {
            Path uploadPath = Paths.get(uploadDir, "payments", String.valueOf(bookingId));
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(safeFileName),
                       StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save payment proof for booking {}: {}", bookingId, e.getMessage(), e);
            throw new BusinessException("Failed to save payment proof. Please try again.");
        }

        String fileUrl = "/api/v1/uploads/payments/" + bookingId + "/" + safeFileName;
        Booking booking = bookingService.uploadPaymentProof(userId, bookingId, fileUrl);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PostMapping("/{bookingId}/verify-payment")
    @Operation(summary = "Verify payment proof", description = "Approve or reject manual transfer proof")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<BookingResponseDTO> verifyPayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody VerifyPaymentRequestDTO request) {
        Long verifierId = authenticationFacade.getCurrentUserId();
        Booking booking = bookingService.verifyPayment(verifierId, bookingId, request.isApproved(), request.getNote());
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PostMapping("/{bookingId}/complete")
    @Operation(summary = "Complete booking", description = "Marks booking as completed (after stay)")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<BookingResponseDTO> completeBooking(@PathVariable Long bookingId) {
        Booking booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @GetMapping("/{bookingId}/invoice")
    @Operation(summary = "Download invoice PDF", description = "Downloads payment invoice for confirmed bookings")
    @PreAuthorize("hasAnyRole('CAMPER', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long bookingId) {
        Long userId = authenticationFacade.getCurrentUserId();
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(bookingId, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm booking payment", description = "Confirms a booking after successful payment")
    public ResponseEntity<BookingResponseDTO> confirmBooking(
            @RequestParam String invoiceNumber,
            @RequestParam String paymentReference) {
        
        Booking confirmedBooking = bookingService.confirmBooking(invoiceNumber, paymentReference);
        return ResponseEntity.ok(bookingMapper.toResponse(confirmedBooking));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancels an existing booking")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long bookingId) {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking cancelledBooking = bookingService.cancelBooking(userId, bookingId);
        return ResponseEntity.ok(bookingMapper.toResponse(cancelledBooking));
    }

    @GetMapping
    @Operation(summary = "Get user bookings", description = "Retrieves user's booking history with filters")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<Page<BookingResponseDTO>> getUserBookings(
            @Parameter(description = "Filter by booking status")
            @RequestParam(required = false) BookingStatus status,
            
            @Parameter(description = "Filter by check-in date from")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            
            @Parameter(description = "Filter by check-in date to")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            
            @PageableDefault(size = 20) Pageable pageable) {
        
        Long userId = authenticationFacade.getCurrentUserId();
        Page<Booking> bookings = bookingService.getUserBookings(userId, status, checkInFrom, checkInTo, pageable);
        
        return ResponseEntity.ok(bookings.map(bookingMapper::toResponse));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Retrieves detailed information about a specific booking")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponseDTO> getBookingDetails(@PathVariable Long bookingId) {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    // Merchant endpoints

    @GetMapping({"/campsite/{campsiteId}", "/campsites/{campsiteId}"})
    @Operation(summary = "Get campsite bookings", description = "Retrieves bookings for a specific campsite (merchant access)")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<BookingResponseDTO>> getCampsiteBookings(
            @PathVariable Long campsiteId,
            
            @Parameter(description = "Filter by booking status")
            @RequestParam(required = false) BookingStatus status,
            
            @Parameter(description = "Filter by check-in date from")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            
            @Parameter(description = "Filter by check-in date to")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<Booking> bookings = bookingService.getCampsiteBookings(campsiteId, status, checkInFrom, checkInTo, pageable);
        
        return ResponseEntity.ok(bookings.map(bookingMapper::toResponse));
    }

    @GetMapping("/campsites/{campsiteId}/{bookingId}")
    @Operation(summary = "Get campsite booking detail", description = "Retrieves booking details for a specific campsite (merchant access)")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<BookingResponseDTO> getCampsiteBookingDetails(
            @PathVariable Long campsiteId,
            @PathVariable Long bookingId
    ) {
        Booking booking = bookingService.getCampsiteBooking(campsiteId, bookingId);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @GetMapping("/availability/spot/{spotId}")
    @Operation(summary = "Check spot availability", description = "Checks if a camping spot is available for given dates")
    public ResponseEntity<Map<String, Object>> checkSpotAvailability(
            @PathVariable String spotId,
            
            @Parameter(description = "Check-in date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            
            @Parameter(description = "Check-out date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        boolean available = bookingService.isSpotAvailable(spotId, checkIn, checkOut);
        
        return ResponseEntity.ok(Map.<String, Object>of(
                "available", available,
                "spotId", spotId,
                "checkIn", checkIn.toString(),
                "checkOut", checkOut.toString()
        ));
    }

    // Admin endpoints for cleanup

    @PostMapping("/admin/cleanup")
    @Operation(summary = "Cleanup expired data", description = "Manually trigger cleanup of expired carts and locks")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupExpiredData() {
        bookingService.cleanupExpiredData();
        return ResponseEntity.ok(Map.of("status", "Cleanup completed successfully"));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Booking service health check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "BookingService",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
