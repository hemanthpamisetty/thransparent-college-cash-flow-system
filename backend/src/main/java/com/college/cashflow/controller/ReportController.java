package com.college.cashflow.controller;

import com.college.cashflow.dto.CategoryBreakdownDTO;
import com.college.cashflow.dto.DailyCashflowDTO;
import com.college.cashflow.dto.DepartmentSummaryDTO;
import com.college.cashflow.dto.MonthlySummaryDTO;
import com.college.cashflow.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/reports/cashflow-trend
     * Daily income/expense/net trend for a date range.
     */
    @GetMapping("/cashflow-trend")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<List<DailyCashflowDTO>> getCashflowTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId,
            Authentication authentication) {
        return ResponseEntity.ok(reportService.getCashflowTrend(startDate, endDate, departmentId, authentication.getName()));
    }

    /**
     * GET /api/reports/category-breakdown
     * Income & expense totals grouped by category.
     */
    @GetMapping("/category-breakdown")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<List<CategoryBreakdownDTO>> getCategoryBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String transactionType,
            Authentication authentication) {
        return ResponseEntity.ok(reportService.getCategoryBreakdown(startDate, endDate, departmentId, transactionType, authentication.getName()));
    }

    /**
     * GET /api/reports/department-summary
     * Per-department income/expense/balance totals (Main Admin only).
     */
    @GetMapping("/department-summary")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<List<DepartmentSummaryDTO>> getDepartmentSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        return ResponseEntity.ok(reportService.getDepartmentSummary(startDate, endDate, authentication.getName()));
    }

    /**
     * GET /api/reports/daily-summary
     * Detailed daily summary for a specific date.
     */
    @GetMapping("/daily-summary")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long departmentId,
            Authentication authentication) {
        return ResponseEntity.ok(reportService.getDailySummary(date, departmentId, authentication.getName()));
    }

    /**
     * GET /api/reports/monthly-summary
     * Month-wise aggregate for a given year.
     */
    @GetMapping("/monthly-summary")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<List<MonthlySummaryDTO>> getMonthlySummary(
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId,
            Authentication authentication) {
        return ResponseEntity.ok(reportService.getMonthlySummary(year, departmentId, authentication.getName()));
    }
}
