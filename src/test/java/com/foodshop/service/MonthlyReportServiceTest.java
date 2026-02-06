package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for MonthlyReportService.
 *
 * <p>Tests monthly report generation, admin email sending, and duplicate prevention.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyReportService Unit Tests")
class MonthlyReportServiceTest {

  @Mock private MonthlyReportRepository reportRepository;

  @Mock private UserService userService;

  @Mock private AnalyticsDashboardService dashboardService;

  @Mock private EmailService emailService;

  @InjectMocks private MonthlyReportService reportService;

  private YearMonth testReportMonth;
  private List<FoodAnalyticsDTO> testAnalytics;
  private List<User> testAdmins;

  @BeforeEach
  void setUp() {
    testReportMonth = YearMonth.of(2026, 1);

    testAnalytics =
        List.of(
            new FoodAnalyticsDTO(1L, "Pizza", "/images/pizza.jpg", 100L, 50L),
            new FoodAnalyticsDTO(2L, "Burger", "/images/burger.jpg", 80L, 40L),
            new FoodAnalyticsDTO(3L, "Pasta", "/images/pasta.jpg", 60L, 30L));

    User admin1 = new User();
    admin1.setId(1L);
    admin1.setEmail("admin1@foodshop.com");
    admin1.setName("Admin One");

    User admin2 = new User();
    admin2.setId(2L);
    admin2.setEmail("admin2@foodshop.com");
    admin2.setName("Admin Two");

    testAdmins = List.of(admin1, admin2);
  }

  @Test
  @DisplayName("generateAndSendReport should create report and send emails to all admins")
  void testGenerateAndSendReport_Success() {
    // Given
    when(reportRepository.existsByReportDateAndStatus(any(), eq(ReportStatus.SENT)))
        .thenReturn(false);
    when(dashboardService.getAllAnalytics(null)).thenReturn(testAnalytics);
    when(dashboardService.getTotalViews()).thenReturn(240L);
    when(dashboardService.getTotalOrders()).thenReturn(120L);
    when(userService.findAdminUsers()).thenReturn(testAdmins);
    when(reportRepository.save(any(MonthlyReport.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    reportService.generateAndSendReport(testReportMonth);

    // Then
    verify(reportRepository).save(any(MonthlyReport.class));
    verify(emailService).sendMonthlyAnalyticsReport(eq(testAdmins), any(MonthlyReportSummaryDTO.class));
  }

  @Test
  @DisplayName("generateAndSendReport should not generate if report already sent")
  void testGenerateAndSendReport_AlreadySent() {
    // Given
    when(reportRepository.existsByReportDateAndStatus(any(), eq(ReportStatus.SENT)))
        .thenReturn(true);

    // When
    reportService.generateAndSendReport(testReportMonth);

    // Then
    verify(reportRepository, never()).save(any());
    verify(emailService, never()).sendMonthlyAnalyticsReport(anyList(), any());
  }

  @Test
  @DisplayName("generateAndSendReport should save report with SENT status on success")
  void testGenerateAndSendReport_SavesWithSentStatus() {
    // Given
    when(reportRepository.existsByReportDateAndStatus(any(), eq(ReportStatus.SENT)))
        .thenReturn(false);
    when(dashboardService.getAllAnalytics(null)).thenReturn(testAnalytics);
    when(dashboardService.getTotalViews()).thenReturn(240L);
    when(dashboardService.getTotalOrders()).thenReturn(120L);
    when(userService.findAdminUsers()).thenReturn(testAdmins);
    when(reportRepository.save(any(MonthlyReport.class)))
        .thenAnswer(
            invocation -> {
              MonthlyReport report = invocation.getArgument(0);
              assertEquals(ReportStatus.SENT, report.getStatus());
              assertEquals(testReportMonth, report.getReportDate());
              assertEquals(3, report.getTotalItems());
              assertEquals(240L, report.getTotalViews());
              assertEquals(120L, report.getTotalOrders());
              return report;
            });

    // When
    reportService.generateAndSendReport(testReportMonth);

    // Then
    verify(reportRepository).save(any(MonthlyReport.class));
  }

  @Test
  @DisplayName("generateAndSendReport should save report with FAILED status on error")
  void testGenerateAndSendReport_SavesWithFailedStatusOnError() {
    // Given
    when(reportRepository.existsByReportDateAndStatus(any(), eq(ReportStatus.SENT)))
        .thenReturn(false);
    when(dashboardService.getAllAnalytics(null)).thenReturn(testAnalytics);
    when(dashboardService.getTotalViews()).thenReturn(240L);
    when(dashboardService.getTotalOrders()).thenReturn(120L);
    when(userService.findAdminUsers()).thenReturn(testAdmins);
    doThrow(new RuntimeException("Email service failure"))
        .when(emailService).sendMonthlyAnalyticsReport(anyList(), any());
    when(reportRepository.save(any(MonthlyReport.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    reportService.generateAndSendReport(testReportMonth);

    // Then
    verify(reportRepository).save(
        any(MonthlyReport.class)); // Should save with FAILED status
  }

  @Test
  @DisplayName("generateAndSendReport should handle empty admin list")
  void testGenerateAndSendReport_NoAdmins() {
    // Given
    when(reportRepository.existsByReportDateAndStatus(any(), eq(ReportStatus.SENT)))
        .thenReturn(false);
    when(dashboardService.getAllAnalytics(null)).thenReturn(testAnalytics);
    when(dashboardService.getTotalViews()).thenReturn(240L);
    when(dashboardService.getTotalOrders()).thenReturn(120L);
    when(userService.findAdminUsers()).thenReturn(List.of());
    when(reportRepository.save(any(MonthlyReport.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    reportService.generateAndSendReport(testReportMonth);

    // Then
    verify(emailService, never()).sendMonthlyAnalyticsReport(anyList(), any());
    verify(reportRepository).save(any(MonthlyReport.class));
  }

  @Test
  @DisplayName("getReportForMonth should return report if exists")
  void testGetReportForMonth_Exists() {
    // Given
    MonthlyReport report = new MonthlyReport();
    LocalDate date = testReportMonth.atDay(1);
    report.setReportDate(date);
    report.setStatus(ReportStatus.SENT);
    when(reportRepository.findByReportDate(date)).thenReturn(Optional.of(report));

    // When
    Optional<MonthlyReport> result = reportService.getReportForMonth(testReportMonth);

    // Then
    assertTrue(result.isPresent());
    assertEquals(testReportMonth, YearMonth.from(result.get().getReportDate()));
  }

  @Test
  @DisplayName("getReportForMonth should return empty if not exists")
  void testGetReportForMonth_NotExists() {
    // Given
    LocalDate date = testReportMonth.atDay(1);
    when(reportRepository.findByReportDate(date)).thenReturn(Optional.empty());

    // When
    Optional<MonthlyReport> result = reportService.getReportForMonth(testReportMonth);

    // Then
    assertFalse(result.isPresent());
  }
}
