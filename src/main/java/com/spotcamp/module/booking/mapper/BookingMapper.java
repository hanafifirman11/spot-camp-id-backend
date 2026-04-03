package com.spotcamp.module.booking.mapper;

import com.spotcamp.module.booking.entity.Booking;
import com.spotcamp.module.booking.entity.BookingItem;
import com.spotcamp.module.booking.entity.ManualTransferBank;
import com.spotcamp.module.booking.dto.BookingResponseDTO;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper for Booking entities and DTOs
 */
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "nights", expression = "java(booking.getNights())")
    @Mapping(target = "canBeCancelled", expression = "java(booking.canBeCancelled())")
    @Mapping(target = "refundAmount", expression = "java(booking.calculateRefundAmount())")
    @Mapping(target = "items", source = "items")
    BookingResponseDTO toResponse(Booking booking);

    List<BookingResponseDTO> toResponseList(List<Booking> bookings);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "subtotal", source = "subtotal")
    BookingResponseDTO.BookingItemResponse toItemResponse(BookingItem item);

    List<BookingResponseDTO.BookingItemResponse> toItemResponseList(List<BookingItem> items);

    @AfterMapping
    default void enrichManualTransfer(Booking booking, @MappingTarget BookingResponseDTO.BookingResponseDTOBuilder builder) {
        if (booking == null || builder == null) {
            return;
        }
        ManualTransferBank bank = ManualTransferBank.fromCode(booking.getPaymentBank());
        if (bank != null) {
            builder.paymentBankName(bank.getDisplayName())
                   .paymentBankAccountNumber(bank.getAccountNumber())
                   .paymentBankAccountName(bank.getAccountName());
        }
        if (booking.getPaymentBankName() != null) {
            builder.paymentBankName(booking.getPaymentBankName());
        }
        if (booking.getPaymentBankAccountNumber() != null) {
            builder.paymentBankAccountNumber(booking.getPaymentBankAccountNumber());
        }
        if (booking.getPaymentBankAccountName() != null) {
            builder.paymentBankAccountName(booking.getPaymentBankAccountName());
        }
    }
}
