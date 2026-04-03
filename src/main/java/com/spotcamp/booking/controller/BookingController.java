package com.spotcamp.booking.controller;

import com.spotcamp.booking.domain.Booking;
import com.spotcamp.booking.domain.BookingStatus;
import com.spotcamp.booking.dto.BookingRequest;
import com.spotcamp.booking.dto.BookingResponse;
import com.spotcamp.booking.dto.CheckoutRequest;
import com.spotcamp.booking.dto.VerifyPaymentRequest;
import com.spotcamp.booking.mapper.BookingMapper;
import com.spotcamp.booking.service.BookingService;
import com.spotcamp.booking.service.InvoicePdfService;
import com.spotcamp.common.security.AuthenticationFacade;
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

/**
 * REST API for booking operations
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking and cart management operations")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final AuthenticationFacade authenticationFacade;
    private final InvoicePdfService invoicePdfService;

    @GetMapping("/cart")
    @Operation(summary = "Get current user's cart", description = "Retrieves the current user's shopping cart")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponse> getCart() {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking cart = bookingService.getOrCreateCart(userId);
        return ResponseEntity.ok(bookingMapper.toResponse(cart));
    }

    @PostMapping("/cart/items")
    @Operation(summary = "Add item to cart", description = "Adds a camping spot or product to the user's cart")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponse> addToCart(@Valid @RequestBody BookingRequest request) {
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
    public ResponseEntity<BookingResponse> removeFromCart(@PathVariable Long itemId) {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking updatedCart = bookingService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(bookingMapper.toResponse(updatedCart));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Proceed to checkout", description = "Moves cart to payment pending status")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
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
    public ResponseEntity<BookingResponse> uploadPaymentProof(
            @PathVariable Long bookingId,
            @RequestParam("file") MultipartFile file) {
        Long userId = authenticationFacade.getCurrentUserId();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileUrl = "/api/v1/uploads/payments/" + bookingId + "/" + fileName;
        try {
            Path uploadPath = Paths.get("uploads/payments/" + bookingId);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Booking booking = bookingService.uploadPaymentProof(userId, bookingId, fileUrl);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PostMapping("/{bookingId}/verify-payment")
    @Operation(summary = "Verify payment proof", description = "Approve or reject manual transfer proof")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<BookingResponse> verifyPayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody VerifyPaymentRequest request) {
        Long verifierId = authenticationFacade.getCurrentUserId();
        Booking booking = bookingService.verifyPayment(verifierId, bookingId, request.isApproved(), request.getNote());
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    @PostMapping("/{bookingId}/complete")
    @Operation(summary = "Complete booking", description = "Marks booking as completed (after stay)")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long bookingId) {
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
    public ResponseEntity<BookingResponse> confirmBooking(
            @RequestParam String invoiceNumber,
            @RequestParam String paymentReference) {
        
        Booking confirmedBooking = bookingService.confirmBooking(invoiceNumber, paymentReference);
        return ResponseEntity.ok(bookingMapper.toResponse(confirmedBooking));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancels an existing booking")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking cancelledBooking = bookingService.cancelBooking(userId, bookingId);
        return ResponseEntity.ok(bookingMapper.toResponse(cancelledBooking));
    }

    @GetMapping
    @Operation(summary = "Get user bookings", description = "Retrieves user's booking history with filters")
    @PreAuthorize("hasRole('CAMPER')")
    public ResponseEntity<Page<BookingResponse>> getUserBookings(
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
    public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable Long bookingId) {
        Long userId = authenticationFacade.getCurrentUserId();
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        return ResponseEntity.ok(bookingMapper.toResponse(booking));
    }

    // Merchant endpoints

    @GetMapping({"/campsite/{campsiteId}", "/campsites/{campsiteId}"})
    @Operation(summary = "Get campsite bookings", description = "Retrieves bookings for a specific campsite (merchant access)")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getCampsiteBookings(
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
    public ResponseEntity<BookingResponse> getCampsiteBookingDetails(
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
