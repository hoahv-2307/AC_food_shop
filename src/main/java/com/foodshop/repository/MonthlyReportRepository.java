package com.foodshop.repository;

import com.foodshop.domain.MonthlyReport;
import com.foodshop.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for MonthlyReport entity.
 * Provides queries for report tracking and duplicate prevention.
 */
@Repository
public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    
    /**
     * Find monthly report by report date.
     *
     * @param reportDate the report date (first day of month)
     * @return optional monthly report
     */
    Optional<MonthlyReport> findByReportDate(LocalDate reportDate);
    
    /**
     * Check if a report exists for given date and status.
     * Used to prevent duplicate report generation.
     *
     * @param reportDate the report date
     * @param status the report status
     * @return true if report exists with given status
     */
    boolean existsByReportDateAndStatus(LocalDate reportDate, ReportStatus status);
}
