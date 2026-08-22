package com.ecommerce.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDTO {
    private UUID id;
    private String reportType;
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscounts;
    private BigDecimal netSales;
    private BigDecimal averageOrderValue;
    private String status;
    private LocalDateTime createdAt;
}
