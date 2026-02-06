package com.foodshop.service;

import com.foodshop.domain.User;
import com.foodshop.dto.MonthlyReportSummaryDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending email notifications.
 *
 * <p>Uses Thymeleaf templates for email content and sends emails asynchronously.
 */
@Service
public class EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final String fromEmail;
  private final String fromName;
  private final String appName;

  public EmailService(
      JavaMailSender mailSender,
      TemplateEngine templateEngine,
      @Value("${spring.mail.from}") String fromEmail,
      @Value("${spring.mail.from-name}") String fromName,
      @Value("${app.name}") String appName) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
    this.fromEmail = fromEmail;
    this.fromName = fromName;
    this.appName = appName;
  }

  /**
   * Sends an order confirmation email asynchronously.
   *
   * @param toEmail recipient email address
   * @param orderNumber the order number
   * @param totalAmount the total order amount
   */
  @Async("emailExecutor")
  public void sendOrderConfirmation(String toEmail, String orderNumber, String totalAmount) {
    try {
      Context context = new Context();
      context.setVariable("orderNumber", orderNumber);
      context.setVariable("totalAmount", totalAmount);
      context.setVariable("appName", appName);

      String htmlContent = templateEngine.process("email/order-confirmation", context);

      sendHtmlEmail(
          toEmail,
          "Order Confirmation - " + orderNumber,
          htmlContent
      );

      LOGGER.info("Order confirmation email sent to: {}", toEmail);
    } catch (Exception e) {
      LOGGER.error("Failed to send order confirmation email to: {}", toEmail, e);
    }
  }

  /**
   * Sends an order status update email asynchronously.
   *
   * @param toEmail recipient email address
   * @param orderNumber the order number
   * @param status the new order status
   */
  @Async("emailExecutor")
  public void sendOrderStatusUpdate(String toEmail, String orderNumber, String status) {
    try {
      Context context = new Context();
      context.setVariable("orderNumber", orderNumber);
      context.setVariable("status", status);
      context.setVariable("appName", appName);

      String htmlContent = templateEngine.process("email/order-status-update", context);

      sendHtmlEmail(
          toEmail,
          "Order Status Update - " + orderNumber,
          htmlContent
      );

      LOGGER.info("Order status update email sent to: {}", toEmail);
    } catch (Exception e) {
      LOGGER.error("Failed to send order status update email to: {}", toEmail, e);
    }
  }

  /**
   * Sends a welcome email to new users asynchronously.
   *
   * @param toEmail recipient email address
   * @param userName the user's name
   */
  @Async("emailExecutor")
  public void sendWelcomeEmail(String toEmail, String userName) {
    try {
      Context context = new Context();
      context.setVariable("userName", userName);
      context.setVariable("appName", appName);

      String htmlContent = templateEngine.process("email/welcome", context);

      sendHtmlEmail(
          toEmail,
          "Welcome to " + appName,
          htmlContent
      );

      LOGGER.info("Welcome email sent to: {}", toEmail);
    } catch (Exception e) {
      LOGGER.error("Failed to send welcome email to: {}", toEmail, e);
    }
  }

  /**
   * Sends monthly analytics report to all admin users.
   *
   * <p>Creates HTML email with analytics summary table showing view and order counts for all food
   * items.
   *
   * @param admins list of admin users to send report to
   * @param summary the monthly report summary data
   */
  @Async("emailExecutor")
  public void sendMonthlyAnalyticsReport(List<User> admins, MonthlyReportSummaryDTO summary) {
    try {
      Context context = new Context();
      context.setVariable("reportMonth", summary.reportMonth());
      context.setVariable("totalItems", summary.totalItems());
      context.setVariable("totalViews", summary.totalViews());
      context.setVariable("totalOrders", summary.totalOrders());
      context.setVariable("items", summary.items());
      context.setVariable("appName", appName);

      String htmlContent = templateEngine.process("email/monthly-analytics-report", context);

      // Send to all admins
      for (User admin : admins) {
        try {
          sendHtmlEmail(
              admin.getEmail(),
              "Monthly Analytics Report - " + summary.reportMonth(),
              htmlContent);
          LOGGER.info("Monthly report sent to admin: {}", admin.getEmail());
        } catch (Exception e) {
          LOGGER.error("Failed to send monthly report to admin: {}", admin.getEmail(), e);
        }
      }

      LOGGER.info(
          "Monthly analytics report for {} sent to {} admins",
          summary.reportMonth(),
          admins.size());
    } catch (Exception e) {
      LOGGER.error("Failed to send monthly analytics report", e);
      throw new RuntimeException("Failed to send monthly analytics report", e);
    }
  }

  private void sendHtmlEmail(String to, String subject, String htmlContent)
      throws MessagingException {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new MessagingException("Failed to set From address", e);
    }
  }
}
