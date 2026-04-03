package com.spotcamp.module.admin.service;

import com.spotcamp.module.admin.dto.AdminDashboardSummaryResponseDTO;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.authuser.repository.UserRepository;
import com.spotcamp.module.booking.repository.BookingRepository;
import com.spotcamp.module.campsite.entity.CampsiteStatus;
import com.spotcamp.module.campsite.repository.CampsiteRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampsiteRepository campsiteRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Test
    void getSummary_ShouldReturnCorrectData() {
        // Arrange
        when(userRepository.countByRole(UserRole.MERCHANT)).thenReturn(10L);
        when(userRepository.countByRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE)).thenReturn(8L);
        when(campsiteRepository.count()).thenReturn(5L);
        when(campsiteRepository.countByStatus(CampsiteStatus.ACTIVE)).thenReturn(3L);
        when(userRepository.countByRole(UserRole.CAMPER)).thenReturn(20L);
        when(bookingRepository.count()).thenReturn(50L);

        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicReference<Double> systemCpu = new AtomicReference<>(0.45d);
        Gauge.builder("system.cpu.usage", systemCpu, AtomicReference::get).register(meterRegistry);
        AdminDashboardService adminDashboardService = new AdminDashboardService(
                userRepository, campsiteRepository, bookingRepository, meterRegistry
        );

        // Act
        AdminDashboardSummaryResponseDTO response = adminDashboardService.getSummary();

        // Assert
        assertEquals(10L, response.getTotalBusinesses());
        assertEquals(8L, response.getActiveBusinesses());
        assertEquals(5L, response.getTotalCampsites());
        assertEquals(3L, response.getActiveCampsites());
        assertEquals(20L, response.getTotalCampers());
        assertEquals(50L, response.getTotalBookings());
        assertEquals(0.45, response.getSystemCpuUsage());
        assertNotNull(response.getTotalMemory());
        assertNotNull(response.getFreeMemory());
        assertNotNull(response.getUptimeSeconds());
    }

    @Test
    void getSummary_ShouldHandleMissingMetricsGracefully() {
        // Arrange
        when(userRepository.countByRole(UserRole.MERCHANT)).thenReturn(10L);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AdminDashboardService adminDashboardService = new AdminDashboardService(
                userRepository, campsiteRepository, bookingRepository, meterRegistry
        );

        // Act
        AdminDashboardSummaryResponseDTO response = adminDashboardService.getSummary();

        // Assert
        assertEquals(10L, response.getTotalBusinesses());
        assertNull(response.getSystemCpuUsage());
        assertNull(response.getProcessCpuUsage());
        assertNotNull(response.getTotalMemory()); // These are from Runtime, not MeterRegistry
    }
}
