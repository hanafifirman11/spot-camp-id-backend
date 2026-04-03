package com.spotcamp.notification.api;

import com.spotcamp.common.security.AuthenticationFacade;
import com.spotcamp.notification.api.dto.NotificationResponse;
import com.spotcamp.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticationFacade authenticationFacade;

    @GetMapping
    @Operation(summary = "Get my notifications")
    @PreAuthorize("hasAnyRole('CAMPER', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<NotificationResponse>> listNotifications() {
        Long userId = authenticationFacade.getCurrentUserId();
        return ResponseEntity.ok(
                notificationService.listByUser(userId).stream()
                        .map(NotificationResponse::from)
                        .toList()
        );
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    @PreAuthorize("hasAnyRole('CAMPER', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        Long userId = authenticationFacade.getCurrentUserId();
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markAsRead(userId, id)));
    }
}
