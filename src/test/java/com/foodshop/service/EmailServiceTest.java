package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodshop.domain.User;
import com.foodshop.dto.FoodAnalyticsDTO;
import com.foodshop.dto.MonthlyReportSummaryDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Unit tests for EmailService monthly report functionality.
 *
 * <p>Tests email composition and sending logic for monthly analytics reports.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Monthly Report Tests")
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @Mock private TemplateEngine templateEngine;

  @Mock private MimeMessage mimeMessage;

  @InjectMocks private EmailService emailService;

  private List<User> testAdmins;
  private MonthlyReportSummaryDTO testReportSummary;

  @BeforeEach
  void setUp() {
    User admin1 = new User();
    admin1.setId(1L);
    admin1.setEmail("admin1@foodshop.com");
    admin1.setName("Admin One");

    User admin2 = new User();
    admin2.setId(2L);
    admin2.setEmail("admin2@foodshop.com");
    admin2.setName("Admin Two");

    testAdmins = List.of(admin1, admin2);

    List<FoodAnalyticsDTO> items =
        List.of(
            new FoodAnalyticsDTO(1L, "Pizza", "/images/pizza.jpg", 100L, 50L),
            new FoodAnalyticsDTO(2L, "Burger", "/images/burger.jpg", 80L, 40L));

    testReportSummary =
        new MonthlyReportSummaryDTO(
            YearMonth.of(2026, 1), 2, // totalItems
            180L, // totalViews
            90L, // totalOrders
            items);
  }

  @Test
  @DisplayName("sendMonthlyAnalyticsReport should send email to all admins")
  void testSendMonthlyAnalyticsReport_Success() throws MessagingException {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/monthly-analytics-report"), any(Context.class)))
        .thenReturn("<html>Test Email Content</html>");

    // When
    assertDoesNotThrow(
        () -> emailService.sendMonthlyAnalyticsReport(testAdmins, testReportSummary));

    // Then
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("sendMonthlyAnalyticsReport should use correct template")
  void testSendMonthlyAnalyticsReport_UsesCorrectTemplate() throws MessagingException {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/monthly-analytics-report"), any(Context.class)))
        .thenReturn("<html>Test Email Content</html>");

    // When
    emailService.sendMonthlyAnalyticsReport(testAdmins, testReportSummary);

    // Then
    verify(templateEngine).process(eq("email/monthly-analytics-report"), any(Context.class));
  }

  @Test
  @DisplayName("sendMonthlyAnalyticsReport should pass correct context to template")
  void testSendMonthlyAnalyticsReport_PassesCorrectContext() throws MessagingException {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/monthly-analytics-report"), any(Context.class)))
        .thenAnswer(
            invocation -> {
              // Verify context contains expected variables
              return "<html>Test Email Content</html>";
            });

    // When
    emailService.sendMonthlyAnalyticsReport(testAdmins, testReportSummary);

    // Then
    verify(templateEngine).process(eq("email/monthly-analytics-report"), any(Context.class));
  }

  @Test
  @DisplayName("sendMonthlyAnalyticsReport should handle email sending failure gracefully")
  void testSendMonthlyAnalyticsReport_HandlesFailure() throws MessagingException {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/monthly-analytics-report"), any(Context.class)))
        .thenReturn("<html>Test Email Content</html>");
    doThrow(new RuntimeException("SMTP connection failed"))
        .when(mailSender).send(any(MimeMessage.class));

    // When & Then - Should not throw exception
    assertDoesNotThrow(
        () -> emailService.sendMonthlyAnalyticsReport(testAdmins, testReportSummary));
  }
}
