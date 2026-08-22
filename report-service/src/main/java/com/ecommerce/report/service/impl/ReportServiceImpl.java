package com.ecommerce.report.service.impl;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.report.dto.GenerateReportRequest;
import com.ecommerce.report.dto.SalesReportDTO;
import com.ecommerce.report.entity.SalesReport;
import com.ecommerce.report.repository.SalesReportRepository;
import com.ecommerce.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final SalesReportRepository salesReportRepository;

    @Override
    @Transactional
    public SalesReportDTO generateSalesReport(GenerateReportRequest request) {
        log.info("Generating sales report type: {} from {} to {}", request.getReportType(), request.getStartDate(), request.getEndDate());

        String type = request.getReportType().toUpperCase();
        LocalDate today = LocalDate.now();

        SalesReport report = salesReportRepository.findByReportDateAndReportType(today, type)
                .orElseGet(() -> SalesReport.builder()
                        .reportType(type)
                        .reportDate(today)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .totalOrders(100)
                        .totalRevenue(BigDecimal.valueOf(10000.00))
                        .totalDiscounts(BigDecimal.valueOf(500.00))
                        .netSales(BigDecimal.valueOf(9500.00))
                        .averageOrderValue(BigDecimal.valueOf(100.00))
                        .status("COMPLETED")
                        .build());

        SalesReport saved = salesReportRepository.save(report);
        log.info("Generated sales report ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reports", key = "#reportId")
    public SalesReportDTO getSalesReportById(UUID reportId) {
        SalesReport report = salesReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesReport", "id", reportId.toString()));
        return mapToDTO(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SalesReportDTO> getSalesReportsByType(String reportType, Pageable pageable) {
        Page<SalesReport> page = salesReportRepository.findByReportType(reportType.toUpperCase(), pageable);
        List<SalesReportDTO> dtos = page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
        return PagedResponse.<SalesReportDTO>builder()
                .content(dtos)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    private SalesReportDTO mapToDTO(SalesReport report) {
        return SalesReportDTO.builder()
                .id(report.getId())
                .reportType(report.getReportType())
                .reportDate(report.getReportDate())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .totalOrders(report.getTotalOrders())
                .totalRevenue(report.getTotalRevenue())
                .totalDiscounts(report.getTotalDiscounts())
                .netSales(report.getNetSales())
                .averageOrderValue(report.getAverageOrderValue())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
