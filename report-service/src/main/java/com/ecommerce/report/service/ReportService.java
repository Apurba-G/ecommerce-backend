package com.ecommerce.report.service;

import com.ecommerce.common.response.PagedResponse;
import com.ecommerce.report.dto.GenerateReportRequest;
import com.ecommerce.report.dto.SalesReportDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportService {

    SalesReportDTO generateSalesReport(GenerateReportRequest request);

    SalesReportDTO getSalesReportById(UUID reportId);

    PagedResponse<SalesReportDTO> getSalesReportsByType(String reportType, Pageable pageable);
}
