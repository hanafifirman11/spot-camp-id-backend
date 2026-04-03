package com.spotcamp.booking.domain;

import com.spotcamp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking entity representing camping reservations and purchases
 * Supports both spot rentals and product purchases in a single transaction
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "campsite_id")
    private Long campsiteId;

    @Column(name = "spot_id", length = 100)
    private String spotId;

    @Column(name = "spot_name", length = 200)
    private String spotName;

    @Column(name = "spot_product_id")
    private Long spotProductId;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.IN_CART;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // Payment info
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 200)
    private String paymentReference;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "payment_bank", length = 20)
    private String paymentBank;

    @Column(name = "payment_bank_name", length = 100)
    private String paymentBankName;

    @Column(name = "payment_bank_account_number", length = 50)
    private String paymentBankAccountNumber;

    @Column(name = "payment_bank_account_name", length = 200)
    private String paymentBankAccountName;

    @Column(name = "payment_unique_code")
    private Integer paymentUniqueCode;

    @Column(name = "payment_amount", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_proof_url", length = 500)
    private String paymentProofUrl;

    @Column(name = "payment_proof_status", length = 20)
    private String paymentProofStatus;

    @Column(name = "payment_proof_uploaded_at")
    private LocalDateTime paymentProofUploadedAt;

    @Column(name = "payment_verified_at")
    private LocalDateTime paymentVerifiedAt;

    @Column(name = "payment_verified_by")
    private Long paymentVerifiedBy;

    @Column(name = "payment_verification_note", length = 500)
    private String paymentVerificationNote;

    // Contact info
    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    // Timestamps
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    /**
     * Calculates the number of nights for this booking
     */
    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Checks if booking is in cart status
     */
    public boolean isInCart() {
        return status == BookingStatus.IN_CART;
    }

    /**
     * Checks if booking is confirmed (paid)
     */
    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.COMPLETED;
    }

    /**
     * Checks if booking is cancelled
     */
    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    /**
     * Checks if booking payment is pending
     */
    public boolean isPaymentPending() {
        return status == BookingStatus.PAYMENT_PENDING;
    }

    /**
     * Checks if booking has expired (cart or payment timeout)
     */
    public boolean hasExpired() {
        return (isInCart() || isPaymentPending()) && expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Moves booking to payment pending status
     */
    public void moveToPaymentPending(String invoiceNumber, String paymentMethod, LocalDateTime paymentExpiry) {
        this.status = BookingStatus.PAYMENT_PENDING;
        this.invoiceNumber = invoiceNumber;
        this.paymentMethod = paymentMethod;
        this.expiresAt = paymentExpiry;
    }

    /**
     * Confirms the booking (successful payment)
     */
    public void confirm(String paymentReference) {
        this.status = BookingStatus.CONFIRMED;
        this.paymentReference = paymentReference;
        this.confirmedAt = LocalDateTime.now();
        this.expiresAt = null; // Clear expiry
    }

    /**
     * Cancels the booking
     */
    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Marks booking as completed (after checkout date)
     */
    public void complete() {
        this.status = BookingStatus.COMPLETED;
    }

    /**
     * Sets cart expiry time (15 minutes from now)
     */
    public void setCartExpiry() {
        this.expiresAt = LocalDateTime.now().plusMinutes(15);
    }

    /**
     * Adds an item to this booking
     */
    public void addItem(Long productId, String productName, String productType, 
                       int quantity, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        BookingItem item = BookingItem.builder()
                .booking(this)
                .productId(productId)
                .productName(productName)
                .productType(productType)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
        
        items.add(item);
        recalculateTotal();
    }

    /**
     * Removes an item from this booking
     */
    public void removeItem(Long itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    /**
     * Recalculates total amount based on items
     */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(BookingItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validates booking data
     */
    public boolean isValid() {
        return userId != null &&
               campsiteId != null &&
               checkInDate != null &&
               checkOutDate != null &&
               checkOutDate.isAfter(checkInDate) &&
               totalAmount != null &&
               totalAmount.compareTo(BigDecimal.ZERO) >= 0 &&
               !items.isEmpty();
    }

    /**
     * Checks if booking can be cancelled
     */
    public boolean canBeCancelled() {
        if (isCancelled() || status == BookingStatus.COMPLETED) {
            return false;
        }
        
        // Can cancel if at least 24 hours before check-in
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return checkInDate.isAfter(tomorrow);
    }

    /**
     * Calculates refund amount based on cancellation policy
     */
    public BigDecimal calculateRefundAmount() {
        if (!canBeCancelled()) {
            return BigDecimal.ZERO;
        }
        
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
        
        // Refund policy: 
        // - 7+ days: 100% refund
        // - 3-6 days: 50% refund  
        // - 1-2 days: 25% refund
        // - Same day: No refund
        
        if (daysUntilCheckIn >= 7) {
            return totalAmount; // 100%
        } else if (daysUntilCheckIn >= 3) {
            return totalAmount.multiply(BigDecimal.valueOf(0.5)); // 50%
        } else if (daysUntilCheckIn >= 1) {
            return totalAmount.multiply(BigDecimal.valueOf(0.25)); // 25%
        } else {
            return BigDecimal.ZERO;
        }
    }
}
