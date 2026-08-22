package com.ecommerce.report.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.report.dto.GenerateReportRequest;
import com.ecommerce.report.dto.SalesReportDTO;
import com.ecommerce.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report Controller", description = "Endpoints for Business Intelligence Analytics and Sales Reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @Operation(summary = "Generate sales report", description = "Aggregates sales performance and revenue metrics for a specified date range")
    public ResponseEntity<ApiResponse<SalesReportDTO>> generateSalesReport(@Valid @RequestBody GenerateReportRequest request) {
        SalesReportDTO report = reportService.generateSalesReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(report, "Sales report generated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID", description = "Retrieves sales report details")
    public ResponseEntity<ApiResponse<SalesReportDTO>> getSalesReportById(@PathVariable("id") UUID id) {
        SalesReportDTO report = reportService.getSalesReportById(id);
        return ResponseEntity.ok(ApiResponse.success(report, "Report retrieved successfully"));
    }

    @GetMapping("/type/{reportType}")
    @Operation(summary = "Get reports by type", description = "Retrieves sales reports filtered by report type e.g. DAILY, MONTHLY")
    public ResponseEntity<ApiResponse<PagedResponse<SalesReportDTO>>> getSalesReportsByType(
            @PathVariable("reportType") String reportType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportDate").descending());
        PagedResponse<SalesReportDTO> reports = reportService.getSalesReportsByType(reportType, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports, "Reports retrieved successfully"));
    }
}
