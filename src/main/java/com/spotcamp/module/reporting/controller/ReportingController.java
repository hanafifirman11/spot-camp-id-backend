package com.spotcamp.module.reporting.controller;

import com.spotcamp.security.AuthenticationFacade;
import com.spotcamp.module.reporting.entity.BookingReport;
import com.spotcamp.module.reporting.entity.RevenueReport;
import com.spotcamp.module.reporting.service.ReportExportService;
import com.spotcamp.module.reporting.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for reporting and analytics
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reporting and analytics operations")
public class ReportingController {

    private final ReportingService reportingService;
    private final AuthenticationFacade authenticationFacade;
    private final ReportExportService reportExportService;

    @GetMapping("/bookings")
    @Operation(summary = "Get booking reports", description = "Retrieve booking reports with filters")
    @PreAuthorize("hasAnyRole('MERCHANT', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<BookingReport>> getBookingReports(
            @Parameter(description = "Campsite ID (null for admin to see all)")
            @RequestParam(required = false) Long campsiteId,

            @Parameter(description = "Business ID (superadmin filter)")
            @RequestParam(required = false) Long businessId,
            
            @Parameter(description = "Booking status filter")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Filter from date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Filter to date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            
            @PageableDefault(size = 50) Pageable pageable) {
        
        // If user is merchant and no campsiteId specified, restrict to their campsites
        if (authenticationFacade.isMerchant() && campsiteId == null) {
            // In real implementation, you would get merchant's campsites
            // For now, we'll assume they can only see their own campsite
            throw new IllegalArgumentException("Merchants must specify campsite ID");
        }
        
        Page<BookingReport> reports = reportingService.getBookingReports(
                campsiteId, businessId, status, fromDate, toDate, pageable);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/bookings/export")
    @Operation(summary = "Export booking reports", description = "Export booking reports as CSV, XLSX, or PDF")
    @PreAuthorize("hasAnyRole('MERCHANT', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> exportBookingReports(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "csv") String format) {

        if (authenticationFacade.isMerchant() && campsiteId == null) {
            throw new IllegalArgumentException("Merchants must specify campsite ID");
        }

        ReportExportService.ReportExportRequest request = new ReportExportService.ReportExportRequest(
                campsiteId, businessId, status, fromDate, toDate, format
        );

        ReportExportService.ExportedFile exported = reportExportService.exportBookingReports(request);

        org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody body = outputStream -> {
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(exported.path())) {
                in.transferTo(outputStream);
            } finally {
                try {
                    java.nio.file.Files.deleteIfExists(exported.path());
                } catch (Exception ignored) {
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exported.filename())
                .contentType(MediaType.parseMediaType(exported.contentType()))
                .body(body);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data", description = "Comprehensive dashboard analytics")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @Parameter(description = "Campsite ID")
            @RequestParam(required = false) Long campsiteId,
            
            @Parameter(description = "Analysis period start")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Analysis period end")  
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        // Default to last 30 days if no dates provided
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        
        Map<String, Object> dashboard = reportingService.getDashboardData(campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/revenue/summary")
    @Operation(summary = "Get revenue summary", description = "Revenue summary for specified period")
    public ResponseEntity<Map<String, Object>> getRevenueSummary(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Map<String, Object> summary = reportingService.getRevenueSummary(campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/revenue/trends")
    @Operation(summary = "Get revenue trends", description = "Revenue trends over time")
    public ResponseEntity<List<RevenueReport>> getRevenueTrends(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam(defaultValue = "monthly") String periodType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        List<RevenueReport> trends = reportingService.getRevenueTrends(
                campsiteId, periodType, fromDate, toDate);
        
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/bookings/monthly")
    @Operation(summary = "Get monthly booking trends", description = "Monthly booking and revenue trends")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year) {
        
        List<Map<String, Object>> trends = reportingService.getMonthlyTrends(campsiteId, year);
        
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/spots/top")
    @Operation(summary = "Get top performing spots", description = "Most popular and profitable spots")
    public ResponseEntity<List<Map<String, Object>>> getTopSpots(
            @RequestParam Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Map<String, Object>> topSpots = reportingService.getTopSpots(
                campsiteId, fromDate, toDate, limit);
        
        return ResponseEntity.ok(topSpots);
    }

    @GetMapping("/customers/analytics")
    @Operation(summary = "Get customer analytics", description = "Customer behavior and demographics")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Map<String, Object> analytics = reportingService.getCustomerAnalytics(
                campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/seasonal")
    @Operation(summary = "Get seasonal performance", description = "Performance by season")
    public ResponseEntity<List<Map<String, Object>>> getSeasonalPerformance(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        List<Map<String, Object>> performance = reportingService.getSeasonalPerformance(
                campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/occupancy/calendar")
    @Operation(summary = "Get occupancy calendar", description = "Daily occupancy data for calendar view")
    public ResponseEntity<List<Map<String, Object>>> getOccupancyCalendar(
            @RequestParam Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        List<Map<String, Object>> calendar = reportingService.getOccupancyCalendar(
                campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(calendar);
    }

    @GetMapping("/occupancy/trends")
    @Operation(summary = "Get occupancy trends", description = "Occupancy rate trends over time")
    public ResponseEntity<List<Map<String, Object>>> getOccupancyTrends(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now().minusYears(1)}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        
        List<Map<String, Object>> trends = reportingService.getOccupancyTrends(campsiteId, fromDate);
        
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/customers/retention")
    @Operation(summary = "Get customer retention metrics", description = "New vs returning customer analysis")
    public ResponseEntity<List<Map<String, Object>>> getCustomerRetention(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now().minusYears(1)}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        
        List<Map<String, Object>> retention = reportingService.getCustomerRetentionMetrics(
                campsiteId, fromDate);
        
        return ResponseEntity.ok(retention);
    }

    @GetMapping("/performance/weekend-weekday")
    @Operation(summary = "Get weekend vs weekday performance", description = "Compare weekend and weekday booking patterns")
    public ResponseEntity<Map<String, Object>> getWeekendVsWeekdayPerformance(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Map<String, Object> performance = reportingService.getWeekendVsWeekdayPerformance(
                campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/revenue/breakdown")
    @Operation(summary = "Get revenue breakdown", description = "Revenue breakdown by product type (spots vs products)")
    public ResponseEntity<Map<String, Object>> getRevenueBreakdown(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Map<String, Object> breakdown = reportingService.getRevenueByProductType(
                campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/bookings/status")
    @Operation(summary = "Get booking status distribution", description = "Distribution of bookings by status")
    public ResponseEntity<Map<String, Long>> getBookingStatusDistribution(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Map<String, Long> distribution = reportingService.getBookingStatusDistribution(
                campsiteId, fromDate, toDate);
        
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/bookings/recent")
    @Operation(summary = "Get recent bookings", description = "Recent booking activity for real-time monitoring")
    public ResponseEntity<List<BookingReport>> getRecentBookings(
            @RequestParam(required = false) Long campsiteId,
            @RequestParam(defaultValue = "24") int hours) {
        
        List<BookingReport> recent = reportingService.getRecentBookings(campsiteId, hours);
        
        return ResponseEntity.ok(recent);
    }

    // Admin-only endpoints

    @GetMapping("/admin/system-analytics")
    @Operation(summary = "Get system-wide analytics", description = "System-wide analytics (admin only)")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemWideAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Map<String, Object> analytics = reportingService.getSystemWideAnalytics(fromDate, toDate);
        
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/admin/campsites/ranking")
    @Operation(summary = "Get campsite performance ranking", description = "Ranking of all campsites by performance (admin only)")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCampsiteRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<Map<String, Object>> ranking = reportingService.getCampsiteRanking(
                fromDate, toDate, limit);
        
        return ResponseEntity.ok(ranking);
    }
}
