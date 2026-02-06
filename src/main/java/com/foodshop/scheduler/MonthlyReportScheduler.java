package com.foodshop.scheduler;

import com.foodshop.service.MonthlyReportService;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automated monthly analytics report generation.
 *
 * <p>Runs at 11 PM on the last day of every month to generate and send analytics reports.
 */
@Component
public class MonthlyReportScheduler {

  private static final Logger logger = LoggerFactory.getLogger(MonthlyReportScheduler.class);

  private final MonthlyReportService reportService;

  public MonthlyReportScheduler(MonthlyReportService reportService) {
    this.reportService = reportService;
  }

  /**
   * Generates and sends monthly analytics report.
   *
   * <p>Cron expression: "0 0 23 L * ?" means:
   *
   * <ul>
   *   <li>0 seconds
   *   <li>0 minutes
   *   <li>23rd hour (11 PM)
   *   <li>L = Last day of month
   *   <li>* = Every month
   *   <li>? = Any day of week
   * </ul>
   *
   * <p>Generates report for the PREVIOUS month (completed month).
   */
  @Scheduled(cron = "0 0 23 L * ?") // 11 PM on last day of month
  public void generateMonthlyReport() {
    logger.info("Monthly report scheduler triggered");

    try {
      // Generate report for previous month (the month that just ended)
      YearMonth previousMonth = YearMonth.now().minusMonths(1);
      logger.info("Generating monthly report for: {}", previousMonth);

      reportService.generateAndSendReport(previousMonth);

      logger.info("Monthly report generation completed successfully");
    } catch (Exception e) {
      logger.error("Failed to generate monthly report", e);
      // Don't rethrow - let scheduler continue for next month
    }
  }
}
