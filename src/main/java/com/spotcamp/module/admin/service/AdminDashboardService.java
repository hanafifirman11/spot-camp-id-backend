package com.spotcamp.module.admin.service;

import com.spotcamp.module.admin.dto.AdminDashboardSummaryResponseDTO;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.authuser.repository.UserRepository;
import com.spotcamp.module.booking.repository.BookingRepository;
import com.spotcamp.module.campsite.entity.CampsiteStatus;
import com.spotcamp.module.campsite.repository.CampsiteRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CampsiteRepository campsiteRepository;
    private final BookingRepository bookingRepository;
    private final MeterRegistry meterRegistry;

    public AdminDashboardSummaryResponseDTO getSummary() {
        long totalBusinesses = userRepository.countByRole(UserRole.MERCHANT);
        long activeBusinesses = userRepository.countByRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE);
        long totalCampsites = campsiteRepository.count();
        long activeCampsites = campsiteRepository.countByStatus(CampsiteStatus.ACTIVE);
        long totalCampers = userRepository.countByRole(UserRole.CAMPER);
        long totalBookings = bookingRepository.count();
        
        // Observability
        Double systemCpu = getGaugeValue("system.cpu.usage");
        Double processCpu = getGaugeValue("process.cpu.usage");
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        TrafficMetrics trafficMetrics = getTrafficMetrics();
        Long diskTotal = toLong(getGaugeValue("disk.total"));
        Long diskFree = toLong(getGaugeValue("disk.free"));
        
        return AdminDashboardSummaryResponseDTO.builder()
            .totalBusinesses(totalBusinesses)
            .activeBusinesses(activeBusinesses)
            .totalCampsites(totalCampsites)
            .activeCampsites(activeCampsites)
            .totalCampers(totalCampers)
            .totalBookings(totalBookings)
            .systemCpuUsage(systemCpu)
            .processCpuUsage(processCpu)
            .totalMemory(Runtime.getRuntime().totalMemory())
            .freeMemory(Runtime.getRuntime().freeMemory())
            .uptimeSeconds(uptime)
            .diskTotal(diskTotal)
            .diskFree(diskFree)
            .httpRequestCount(trafficMetrics.requestCount())
            .httpErrorCount(trafficMetrics.errorCount())
            .httpAverageLatencyMs(trafficMetrics.averageLatencyMs())
            .build();
    }

    private Double getGaugeValue(String metricName) {
        try {
            return meterRegistry.get(metricName).gauge().value();
        } catch (Exception e) {
            return null;
        }
    }

    private TrafficMetrics getTrafficMetrics() {
        double totalCount = 0;
        double errorCount = 0;
        double totalTimeMs = 0;
        for (Timer timer : meterRegistry.find("http.server.requests").timers()) {
            long count = timer.count();
            if (count == 0) {
                continue;
            }
            totalCount += count;
            totalTimeMs += timer.totalTime(TimeUnit.MILLISECONDS);
            String status = timer.getId().getTag("status");
            if (status != null && (status.startsWith("4") || status.startsWith("5"))) {
                errorCount += count;
            }
        }
        Long requests = totalCount > 0 ? (long) totalCount : 0L;
        Long errors = errorCount > 0 ? (long) errorCount : 0L;
        Double avgLatency = totalCount > 0 ? totalTimeMs / totalCount : null;
        return new TrafficMetrics(requests, errors, avgLatency);
    }

    private Long toLong(Double value) {
        if (value == null || value.isNaN()) {
            return null;
        }
        return value.longValue();
    }

    private record TrafficMetrics(Long requestCount, Long errorCount, Double averageLatencyMs) {}
}
