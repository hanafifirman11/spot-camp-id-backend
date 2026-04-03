package com.spotcamp.module.booking.service;

import com.spotcamp.module.booking.entity.Booking;
import com.spotcamp.module.booking.entity.BookingStatus;
import com.spotcamp.module.booking.entity.InventoryLock;
import com.spotcamp.module.booking.entity.InventoryLock.LockType;
import com.spotcamp.module.booking.entity.ManualTransferBank;
import com.spotcamp.module.booking.repository.BookingRepository;
import com.spotcamp.module.booking.repository.InventoryLockRepository;
import com.spotcamp.module.campsite.entity.Campsite;
import com.spotcamp.module.campsite.repository.CampsiteRepository;
import com.spotcamp.module.inventory.entity.Product;
import com.spotcamp.module.inventory.service.ProductService;
import com.spotcamp.module.inventory.service.ProductStockService;
import com.spotcamp.module.notification.entity.Notification;
import com.spotcamp.module.notification.service.NotificationService;
import com.spotcamp.module.payment.bank.account.MerchantBankAccount;
import com.spotcamp.module.payment.bank.account.MerchantBankAccountRepository;
import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for booking operations with pessimistic locking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryLockRepository inventoryLockRepository;
    private final ProductService productService;
    private final ProductStockService productStockService;
    private final MerchantBankAccountRepository merchantBankAccountRepository;
    private final CampsiteRepository campsiteRepository;
    private final NotificationService notificationService;

    /**
     * Get or create user's cart booking
     */
    public Booking getOrCreateCart(Long userId) {
        // First try to find existing cart without lock (faster)
        Optional<Booking> existingCart = bookingRepository.findByUserIdAndStatus(userId, BookingStatus.IN_CART);
        
        if (existingCart.isPresent()) {
            Booking cart = existingCart.get();
            
            // Check if cart has expired
            if (cart.hasExpired()) {
                // Clean up expired cart and its locks
                cleanupExpiredCart(cart);
                return createNewCart(userId);
            }
            
            return cart;
        }
        
        return createNewCart(userId);
    }

    /**
     * Add item to cart with pessimistic locking
     */
    public Booking addItemToCart(Long userId, Long productId, String spotId, 
                                LocalDate checkIn, LocalDate checkOut, int quantity) {
        
        // Validate input
        validateBookingDates(checkIn, checkOut);
        
        // Get product details
        Product product = productService.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found"));
        
        // Get user's cart with pessimistic lock
        Booking cart = bookingRepository.findUserCartWithLock(userId)
                .orElseGet(() -> createNewCart(userId));
        
        // Check availability and create lock
        if (!checkAvailabilityAndCreateLock(cart.getId(), product, spotId, checkIn, checkOut, quantity)) {
            throw new BusinessException("Item not available for selected dates/quantity");
        }
        
        // Add item to booking
        cart.addItem(productId, product.getName(), product.getType().name(), quantity, product.getPrice());
        cart.setCheckInDate(checkIn);
        cart.setCheckOutDate(checkOut);
        cart.setCampsiteId(product.getCampsiteId());
        cart.setSpotId(spotId);
        if (product.isRentalSpot()) {
            cart.setSpotName(product.getName());
            cart.setSpotProductId(product.getId());
        }
        cart.setCartExpiry(); // Reset cart expiry
        
        return bookingRepository.save(cart);
    }

    /**
     * Remove item from cart
     */
    public Booking removeItemFromCart(Long userId, Long itemId) {
        Booking cart = bookingRepository.findUserCartByItemId(userId, itemId)
                .orElseThrow(() -> new BusinessException("Item not found in cart"));
        
        // Remove item and its locks
        cart.removeItem(itemId);
        cart.recalculateTotal();
        
        // Clean up related locks
        // Note: In real implementation, we would need to track which locks belong to which items
        // For simplicity, we'll remove all cart locks for this booking
        inventoryLockRepository.deleteByBookingId(cart.getId());
        
        // Recreate locks for remaining items
        recreateLocksForCartItems(cart);
        
        return bookingRepository.save(cart);
    }

    /**
     * Proceed to checkout (move cart to payment pending)
     */
    public Booking proceedToCheckout(Long userId, String contactName, String contactEmail, 
                                   String contactPhone, String specialRequests, String paymentMethod,
                                   String paymentChannel) {
        
        Booking cart = bookingRepository.findUserCartWithLock(userId)
                .orElseThrow(() -> new BusinessException("Cart not found"));
        
        if (cart.hasExpired()) {
            throw new BusinessException("Cart has expired. Please add items again.");
        }
        
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }
        
        // Validate final availability before checkout
        if (!validateFinalAvailability(cart)) {
            throw new BusinessException("Some items are no longer available");
        }
        
        // Set contact information
        cart.setContactName(contactName);
        cart.setContactEmail(contactEmail);
        cart.setContactPhone(contactPhone);
        cart.setSpecialRequests(specialRequests);
        
        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        LocalDateTime paymentExpiry;
        if ("MANUAL_TRANSFER".equalsIgnoreCase(paymentMethod)) {
            ManualTransferBank bank = ManualTransferBank.fromCode(paymentChannel);
            if (bank == null) {
                throw new BusinessException("Invalid payment channel");
            }
            if (cart.getCampsiteId() != null) {
                Optional<Campsite> campsite = campsiteRepository.findById(cart.getCampsiteId());
                if (campsite.isPresent()) {
                    Long merchantId = campsite.get().getOwnerId();
                    List<MerchantBankAccount> accounts = merchantBankAccountRepository
                        .findByMerchantIdAndBankCodeIgnoreCaseAndIsActiveTrue(merchantId, bank.getCode());
                    accounts.stream().findFirst().ifPresent(account -> {
                        cart.setPaymentBank(account.getBankCode());
                        cart.setPaymentBankName(account.getBankCode());
                        cart.setPaymentBankAccountNumber(account.getAccountNumber());
                        cart.setPaymentBankAccountName(account.getAccountName());
                    });
                }
            }
            int uniqueCode = ThreadLocalRandom.current().nextInt(100, 1000);
            BigDecimal baseAmount = cart.getTotalAmount() == null ? BigDecimal.ZERO : cart.getTotalAmount();
            BigDecimal paymentAmount = baseAmount.add(BigDecimal.valueOf(uniqueCode));

            if (cart.getPaymentBank() == null) {
                cart.setPaymentBank(bank.getCode());
            }
            if (cart.getPaymentBankName() == null) {
                cart.setPaymentBankName(bank.getDisplayName());
            }
            if (cart.getPaymentBankAccountNumber() == null) {
                cart.setPaymentBankAccountNumber(bank.getAccountNumber());
            }
            if (cart.getPaymentBankAccountName() == null) {
                cart.setPaymentBankAccountName(bank.getAccountName());
            }
            cart.setPaymentUniqueCode(uniqueCode);
            cart.setPaymentAmount(paymentAmount);
            cart.setPaymentProofStatus("WAITING_UPLOAD");
            paymentExpiry = LocalDateTime.now().plusDays(2);
        } else {
            paymentExpiry = LocalDateTime.now().plusMinutes(30);
        }

        // Move to payment pending with expiry
        cart.moveToPaymentPending(invoiceNumber, paymentMethod, paymentExpiry);
        
        return bookingRepository.save(cart);
    }

    /**
     * Confirm booking (payment successful)
     */
    public Booking confirmBooking(String invoiceNumber, String paymentReference) {
        Booking booking = bookingRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        return confirmBookingInternal(booking, paymentReference);
    }

    @Transactional
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed bookings can be completed");
        }
        booking.complete();
        bookingRepository.save(booking);
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    @Transactional
    public Booking uploadPaymentProof(Long userId, Long bookingId, String proofUrl) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("Unauthorized access to booking");
        }
        if (!booking.isPaymentPending()) {
            throw new BusinessException("Booking is not awaiting payment");
        }
        booking.setPaymentProofUrl(proofUrl);
        booking.setPaymentProofStatus("UPLOADED");
        booking.setPaymentProofUploadedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    @Transactional
    public Booking verifyPayment(Long verifierId, Long bookingId, boolean approved, String note) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        if (!booking.isPaymentPending()) {
            throw new BusinessException("Booking is not awaiting payment");
        }
        if (approved) {
            if (booking.getPaymentProofUrl() == null || booking.getPaymentProofUrl().isBlank()) {
                throw new BusinessException("Payment proof is required before verification");
            }
            booking.setPaymentProofStatus("VERIFIED");
            booking.setPaymentVerifiedAt(LocalDateTime.now());
            booking.setPaymentVerifiedBy(verifierId);
            booking.setPaymentVerificationNote(note);
            Booking confirmed = confirmBookingInternal(booking, "MANUAL_TRANSFER");
            return bookingRepository.findById(confirmed.getId())
                    .orElseThrow(() -> new BusinessException("Booking not found"));
        }
        booking.setPaymentProofStatus("REJECTED");
        booking.setPaymentVerifiedAt(LocalDateTime.now());
        booking.setPaymentVerifiedBy(verifierId);
        booking.setPaymentVerificationNote(note);
        bookingRepository.save(booking);
        String bookingLabel = booking.getId() != null ? "Booking #" + booking.getId() : "Booking";
        notificationService.createNotification(
                booking.getUserId(),
                Notification.NotificationType.PAYMENT_PROOF_REUPLOAD,
                bookingLabel + " · Bukti transfer perlu diulang",
                "Bukti transfer belum jelas untuk " + bookingLabel + ". Mohon upload ulang bukti pembayaran agar booking dapat diverifikasi.",
                Notification.NotificationPriority.HIGH,
                Map.of(
                        "bookingId", booking.getId(),
                        "invoice", booking.getInvoiceNumber(),
                        "url", "/bookings/" + booking.getId()
                ),
                "BOOKING",
                booking.getId()
        );
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    private Booking confirmBookingInternal(Booking booking, String paymentReference) {
        if (!booking.isPaymentPending()) {
            throw new BusinessException("Booking is not in payment pending status");
        }

        if (booking.hasExpired()) {
            throw new BusinessException("Payment window has expired");
        }

        // Reduce stock for sale products with pessimistic locking
        applySaleStockReduction(booking);

        // Confirm the booking
        booking.confirm(paymentReference);

        // Convert cart locks to confirmed locks
        confirmInventoryLocks(booking);

        return bookingRepository.save(booking);
    }

    /**
     * Cancel booking
     */
    public Booking cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("Unauthorized access to booking");
        }
        
        if (!booking.canBeCancelled()) {
            throw new BusinessException("Booking cannot be cancelled");
        }

        boolean wasConfirmed = booking.isConfirmed();

        // Cancel the booking
        booking.cancel();

        if (wasConfirmed) {
            restoreSaleStock(booking);
        }

        // Release inventory locks
        inventoryLockRepository.deleteByBookingId(bookingId);
        
        log.info("Booking {} cancelled by user {}", bookingId, userId);
        
        return bookingRepository.save(booking);
    }

    /**
     * Get user's bookings with filters
     */
    @Transactional(readOnly = true)
    public Page<Booking> getUserBookings(Long userId, BookingStatus status, 
                                       LocalDate checkInFrom, LocalDate checkInTo, Pageable pageable) {
        return bookingRepository.findUserBookings(userId, status, checkInFrom, checkInTo, pageable);
    }

    /**
     * Get a single booking for a user (camper access).
     */
    @Transactional(readOnly = true)
    public Booking getUserBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Booking", bookingId);
        }

        return booking;
    }

    /**
     * Get campsite's bookings (for merchants)
     */
    @Transactional(readOnly = true)
    public Page<Booking> getCampsiteBookings(Long campsiteId, BookingStatus status, 
                                           LocalDate checkInFrom, LocalDate checkInTo, Pageable pageable) {
        return bookingRepository.findCampsiteBookings(campsiteId, status, checkInFrom, checkInTo, pageable);
    }

    /**
     * Get a single booking for a campsite (merchant access).
     */
    @Transactional(readOnly = true)
    public Booking getCampsiteBooking(Long campsiteId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getCampsiteId().equals(campsiteId)) {
            throw new ResourceNotFoundException("Booking", bookingId);
        }

        return booking;
    }

    /**
     * Check spot availability for date range
     */
    @Transactional(readOnly = true)
    public boolean isSpotAvailable(String spotId, LocalDate checkIn, LocalDate checkOut) {
        return inventoryLockRepository.isSpotAvailableForDateRange(spotId, checkIn, checkOut) &&
               !bookingRepository.isSpotBookedForDateRange(spotId, checkIn, checkOut);
    }

    /**
     * Cleanup expired carts and locks (scheduled task)
     */
    public void cleanupExpiredData() {
        LocalDateTime cutoffTime = LocalDateTime.now();
        
        // Find expired carts
        List<Booking> expiredCarts = bookingRepository.findExpiredCartBookings(cutoffTime);
        for (Booking cart : expiredCarts) {
            cleanupExpiredCart(cart);
        }
        
        // Find expired payment bookings
        List<Booking> expiredPayments = bookingRepository.findExpiredPaymentBookings(cutoffTime);
        for (Booking booking : expiredPayments) {
            booking.cancel();
            if (booking.isConfirmed()) {
                restoreSaleStock(booking);
            }
            inventoryLockRepository.deleteByBookingId(booking.getId());
            bookingRepository.save(booking);
        }
        
        // Clean up orphaned locks
        inventoryLockRepository.deleteExpiredCartLocks(cutoffTime);
        
        log.info("Cleaned up {} expired carts and {} expired payments", 
                expiredCarts.size(), expiredPayments.size());
    }

    // Private helper methods

    private Booking createNewCart(Long userId) {
        Booking cart = Booking.builder()
                .userId(userId)
                .status(BookingStatus.IN_CART)
                .totalAmount(BigDecimal.ZERO)
                .build();
        
        cart.setCartExpiry();
        return bookingRepository.save(cart);
    }

    private void cleanupExpiredCart(Booking cart) {
        inventoryLockRepository.deleteByBookingId(cart.getId());
        bookingRepository.delete(cart);
        log.info("Cleaned up expired cart for user {}", cart.getUserId());
    }

    private boolean checkAvailabilityAndCreateLock(Long bookingId, Product product, String spotId,
                                                 LocalDate checkIn, LocalDate checkOut, int quantity) {

        Product lockedProduct = product;
        if (product.isSale()) {
            lockedProduct = productService.getProductForUpdate(product.getId());
        }
        
        // For rental products (spots), check date availability
        if (lockedProduct.isRentalSpot()) {
            if (spotId == null) {
                throw new BusinessException("Spot ID required for rental map products");
            }
            
            // Check if spot is available
            if (!isSpotAvailable(spotId, checkIn, checkOut)) {
                return false;
            }
            
            // Create lock for the spot
            InventoryLock lock = InventoryLock.builder()
                    .productId(lockedProduct.getId())
                    .bookingId(bookingId)
                    .spotId(spotId)
                    .quantity(1)
                    .lockType(LockType.CART)
                    .startDate(checkIn)
                    .endDate(checkOut)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            
            inventoryLockRepository.save(lock);
        } else if (lockedProduct.isRentalItem()) {
            long confirmedLocks = inventoryLockRepository.countLockedQuantity(
                    lockedProduct.getId(), LockType.CONFIRMED, checkIn, checkOut).orElse(0L);

            long cartLocks = inventoryLockRepository.countLockedQuantity(
                    lockedProduct.getId(), LockType.CART, checkIn, checkOut).orElse(0L);

            long availableQuantity = productStockService.getAvailableQuantity(lockedProduct) - confirmedLocks - cartLocks;

            if (availableQuantity < quantity) {
                return false;
            }

            InventoryLock lock = InventoryLock.builder()
                    .productId(lockedProduct.getId())
                    .bookingId(bookingId)
                    .quantity(quantity)
                    .lockType(LockType.CART)
                    .startDate(checkIn)
                    .endDate(checkOut)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            inventoryLockRepository.save(lock);
        } else {
            // For sale products, check quantity availability
            long confirmedLocks = inventoryLockRepository.countLockedQuantity(
                    lockedProduct.getId(), LockType.CONFIRMED, null, null).orElse(0L);
            
            long cartLocks = inventoryLockRepository.countLockedQuantity(
                    lockedProduct.getId(), LockType.CART, null, null).orElse(0L);
            
            long availableQuantity = productStockService.getAvailableQuantity(lockedProduct) - confirmedLocks - cartLocks;
            
            if (availableQuantity < quantity) {
                return false;
            }
            
            // Create lock for the quantity
            InventoryLock lock = InventoryLock.builder()
                    .productId(lockedProduct.getId())
                    .bookingId(bookingId)
                    .quantity(quantity)
                    .lockType(LockType.CART)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            
            inventoryLockRepository.save(lock);
        }
        
        return true;
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = LocalDate.now();
        
        if (checkIn.isBefore(today)) {
            throw new BusinessException("Check-in date cannot be in the past");
        }
        
        if (checkOut.isBefore(checkIn.plusDays(1))) {
            throw new BusinessException("Check-out date must be at least 1 day after check-in");
        }
    }

    private boolean validateFinalAvailability(Booking booking) {
        for (var item : booking.getItems()) {
            Product product = productService.findById(item.getProductId()).orElse(null);
            if (product == null) {
                return false;
            }
            
            if (product.isRentalSpot()) {
                if (!isSpotAvailable(booking.getSpotId(), booking.getCheckInDate(), booking.getCheckOutDate())) {
                    return false;
                }
            } else if (product.isRentalItem()) {
                long confirmedLocks = inventoryLockRepository.countLockedQuantity(
                        product.getId(), LockType.CONFIRMED, booking.getCheckInDate(), booking.getCheckOutDate()
                ).orElse(0L);

                if (productStockService.getAvailableQuantity(product) - confirmedLocks < item.getQuantity()) {
                    return false;
                }
            } else {
                long confirmedLocks = inventoryLockRepository.countLockedQuantity(
                        product.getId(), LockType.CONFIRMED, null, null).orElse(0L);
                
                if (productStockService.getAvailableQuantity(product) - confirmedLocks < item.getQuantity()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void confirmInventoryLocks(Booking booking) {
        LocalDateTime confirmExpiry = booking.getCheckOutDate().plusDays(1).atStartOfDay();
        inventoryLockRepository.confirmRentalLocksForBooking(booking.getId(), confirmExpiry);
        inventoryLockRepository.deleteSaleLocksForBooking(booking.getId());
    }

    private void applySaleStockReduction(Booking booking) {
        Map<Long, Integer> saleQuantities = booking.getItems().stream()
                .filter(item -> "SALE".equalsIgnoreCase(item.getProductType()))
                .collect(Collectors.toMap(
                        item -> item.getProductId(),
                        item -> item.getQuantity() != null ? item.getQuantity() : 0,
                        Integer::sum
                ));

        if (saleQuantities.isEmpty()) {
            return;
        }

        List<Long> productIds = new ArrayList<>(saleQuantities.keySet());
        productIds.sort(Long::compareTo);

        for (Long productId : productIds) {
            int quantity = saleQuantities.getOrDefault(productId, 0);
            if (quantity <= 0) {
                continue;
            }

            Product product = productService.getProductForUpdate(productId);
            if (!product.isSale()) {
                continue;
            }
            productStockService.adjustStock(product, -quantity, "Booking confirmed", booking.getId(), booking.getUserId());
        }
    }

    private void restoreSaleStock(Booking booking) {
        Map<Long, Integer> saleQuantities = booking.getItems().stream()
                .filter(item -> "SALE".equalsIgnoreCase(item.getProductType()))
                .collect(Collectors.toMap(
                        item -> item.getProductId(),
                        item -> item.getQuantity() != null ? item.getQuantity() : 0,
                        Integer::sum
                ));

        if (saleQuantities.isEmpty()) {
            return;
        }

        List<Long> productIds = new ArrayList<>(saleQuantities.keySet());
        productIds.sort(Long::compareTo);

        for (Long productId : productIds) {
            int quantity = saleQuantities.getOrDefault(productId, 0);
            if (quantity <= 0) {
                continue;
            }

            Product product = productService.getProductForUpdate(productId);
            if (!product.isSale()) {
                continue;
            }
            productStockService.adjustStock(product, quantity, "Booking cancelled", booking.getId(), booking.getUserId());
        }
    }

    private void recreateLocksForCartItems(Booking cart) {
        // This is a simplified implementation
        // In practice, you'd need to track which items correspond to which locks
        for (var item : cart.getItems()) {
            Product product = productService.findById(item.getProductId()).orElse(null);
            if (product != null) {
                checkAvailabilityAndCreateLock(cart.getId(), product, cart.getSpotId(),
                        cart.getCheckInDate(), cart.getCheckOutDate(), item.getQuantity());
            }
        }
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
