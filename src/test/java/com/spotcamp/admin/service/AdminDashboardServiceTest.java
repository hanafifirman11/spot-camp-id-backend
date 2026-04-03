package com.spotcamp.admin.service;

import com.spotcamp.admin.api.dto.AdminDashboardSummaryResponse;
import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.authuser.domain.UserStatus;
import com.spotcamp.authuser.repository.UserRepository;
import com.spotcamp.booking.repository.BookingRepository;
import com.spotcamp.campsite.domain.CampsiteStatus;
import com.spotcamp.campsite.repository.CampsiteRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.search.Search;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampsiteRepository campsiteRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getSummary_ShouldReturnCorrectData() {
        // Arrange
        when(userRepository.countByRole(UserRole.MERCHANT)).thenReturn(10L);
        when(userRepository.countByRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE)).thenReturn(8L);
        when(campsiteRepository.count()).thenReturn(5L);
        when(campsiteRepository.countByStatus(CampsiteStatus.ACTIVE)).thenReturn(3L);
        when(userRepository.countByRole(UserRole.CAMPER)).thenReturn(20L);
        when(bookingRepository.count()).thenReturn(50L);

        // Mock MeterRegistry chain for CPU usage
        RequiredSearch searchMock = mock(RequiredSearch.class);
        Gauge gaugeMock = mock(Gauge.class);
        
        // Use doReturn to be safe
        doReturn(searchMock).when(meterRegistry).get("system.cpu.usage");
        when(searchMock.gauge()).thenReturn(gaugeMock);
        when(gaugeMock.value()).thenReturn(0.45);

        // Act
        AdminDashboardSummaryResponse response = adminDashboardService.getSummary();

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
        
        // Mock MeterRegistry to throw exception or return null
        when(meterRegistry.get(any())).thenThrow(new RuntimeException("Metric not found"));

        // Act
        AdminDashboardSummaryResponse response = adminDashboardService.getSummary();

        // Assert
        assertEquals(10L, response.getTotalBusinesses());
        assertNull(response.getSystemCpuUsage());
        assertNull(response.getProcessCpuUsage());
        assertNotNull(response.getTotalMemory()); // These are from Runtime, not MeterRegistry
    }
}
