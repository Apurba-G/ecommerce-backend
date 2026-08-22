package com.ecommerce.report.repository;

import com.ecommerce.report.entity.SalesReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesReportRepository extends JpaRepository<SalesReport, UUID> {

    Optional<SalesReport> findByReportDateAndReportType(LocalDate reportDate, String reportType);

    Page<SalesReport> findByReportType(String reportType, Pageable pageable);
}
