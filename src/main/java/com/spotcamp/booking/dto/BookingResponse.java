package com.spotcamp.booking.dto;

import com.spotcamp.booking.domain.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for booking information
 */
@Data
@Builder
public class BookingResponse {
    
    private Long id;
    private Long userId;
    private Long campsiteId;
    private String spotId;
    private String spotName;
    private Long spotProductId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentReference;
    private String invoiceNumber;
    private String paymentBank;
    private Integer paymentUniqueCode;
    private BigDecimal paymentAmount;
    private String paymentProofUrl;
    private String paymentProofStatus;
    private LocalDateTime paymentProofUploadedAt;
    private LocalDateTime paymentVerifiedAt;
    private Long paymentVerifiedBy;
    private String paymentVerificationNote;
    private String paymentBankName;
    private String paymentBankAccountNumber;
    private String paymentBankAccountName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String specialRequests;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private List<BookingItemResponse> items;
    private boolean canBeCancelled;
    private BigDecimal refundAmount;
    
    @Data
    @Builder
    public static class BookingItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productType;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
