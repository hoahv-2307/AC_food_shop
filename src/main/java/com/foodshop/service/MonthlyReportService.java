package com.foodshop.service;

import com.foodshop.domain.MonthlyReport;
import com.foodshop.domain.ReportStatus;
import com.foodshop.domain.User;
import com.foodshop.dto.FoodAnalyticsDTO;
import com.foodshop.dto.MonthlyReportSummaryDTO;
import com.foodshop.repository.MonthlyReportRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for generating and managing monthly analytics reports.
 *
 * <p>Handles report generation, duplicate prevention, and email sending to admin users.
 */
@Service
public class MonthlyReportService {

  private static final Logger logger = LoggerFactory.getLogger(MonthlyReportService.class);

  private final MonthlyReportRepository reportRepository;
  private final UserService userService;
  private final AnalyticsDashboardService dashboardService;
  private final EmailService emailService;

  public MonthlyReportService(
      MonthlyReportRepository reportRepository,
      UserService userService,
      AnalyticsDashboardService dashboardService,
      EmailService emailService) {
    this.reportRepository = reportRepository;
    this.userService = userService;
    this.dashboardService = dashboardService;
    this.emailService = emailService;
  }

  /**
   * Generates and sends monthly analytics report for the specified month.
   *
   * <p>Process:
   *
   * <ol>
   *   <li>Check if report already sent (duplicate prevention)
   *   <li>Fetch analytics data from dashboard service
   *   <li>Create report summary DTO
   *   <li>Fetch all admin users
   *   <li>Send email to all admins
   *   <li>Save report with SENT status (or FAILED if error)
   * </ol>
   *
   * @param reportMonth the month to generate report for
   */
  @Transactional
  public void generateAndSendReport(YearMonth reportMonth) {
    logger.info("Starting monthly report generation for {}", reportMonth);

    // Check if report already sent
    LocalDate reportDate = reportMonth.atDay(1);
    if (reportRepository.existsByReportDateAndStatus(reportDate, ReportStatus.SENT)) {
      logger.info("Report for {} already sent, skipping", reportMonth);
      return;
    }

    MonthlyReport report = new MonthlyReport();
    report.setReportDate(reportDate);
    report.setStatus(ReportStatus.GENERATING);

    try {
      // Fetch analytics data
      List<FoodAnalyticsDTO> analyticsData = dashboardService.getAllAnalytics(null);
      Long totalViews = dashboardService.getTotalViews();
      Long totalOrders = dashboardService.getTotalOrders();

      // Create report summary
      MonthlyReportSummaryDTO summary =
          new MonthlyReportSummaryDTO(
              reportMonth,
              analyticsData.size(),
              totalViews,
              totalOrders,
              analyticsData);

      // Save statistics to report
      report.setTotalItems(analyticsData.size());
      report.setTotalViews(totalViews);
      report.setTotalOrders(totalOrders);

      // Fetch admin users
      List<User> admins = userService.findAdminUsers();
      logger.info("Found {} admin users to send report to", admins.size());

      if (admins.isEmpty()) {
        logger.warn("No admin users found to send report to");
        report.setStatus(ReportStatus.SENT); // Still mark as sent even if no admins
        report.setErrorMessage("No admin users found");
      } else {
        // Send email
        emailService.sendMonthlyAnalyticsReport(admins, summary);
        report.setStatus(ReportStatus.SENT);
        logger.info("Monthly report for {} sent successfully to {} admins", reportMonth, admins.size());
      }

    } catch (Exception e) {
      logger.error("Failed to generate/send monthly report for {}", reportMonth, e);
      report.setStatus(ReportStatus.FAILED);
      report.setErrorMessage(e.getMessage());
    }

    // Save report
    reportRepository.save(report);
  }

  /**
   * Retrieves the report for a specific month.
   *
   * @param reportMonth the month to get report for
   * @return the monthly report if exists
   */
  @Transactional(readOnly = true)
  public Optional<MonthlyReport> getReportForMonth(YearMonth reportMonth) {
    LocalDate reportDate = reportMonth.atDay(1);
    return reportRepository.findByReportDate(reportDate);
  }

  /**
   * Retrieves all reports ordered by date descending.
   *
   * @return list of all reports
   */
  @Transactional(readOnly = true)
  public List<MonthlyReport> getAllReports() {
    return reportRepository.findAll();
  }
}
