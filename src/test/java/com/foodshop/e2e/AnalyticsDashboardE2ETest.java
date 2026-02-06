package com.foodshop.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

/**
 * End-to-end test for analytics dashboard using Selenium.
 *
 * <p>Tests complete user journey: admin login → navigate to dashboard → verify analytics display
 * → test sorting.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Analytics Dashboard E2E Tests")
class AnalyticsDashboardE2ETest {

  @LocalServerPort private int port;

  private WebDriver driver;
  private String baseUrl;

  @BeforeAll
  static void setupClass() {
    WebDriverManager.chromedriver().setup();
  }

  @BeforeEach
  void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    driver = new ChromeDriver(options);
    baseUrl = "http://localhost:" + port;
  }

  @AfterEach
  void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  @DisplayName("Admin should see analytics dashboard with all food items")
  void testViewAnalyticsDashboard() {
    // Given - Admin logs in
    loginAsAdmin();

    // When - Navigate to analytics dashboard
    driver.get(baseUrl + "/admin/analytics");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("analytics-table")));

    // Then - Verify dashboard page loads
    assertTrue(driver.getTitle().contains("Analytics Dashboard"));

    // Verify analytics table exists
    WebElement analyticsTable = driver.findElement(By.className("analytics-table"));
    assertTrue(analyticsTable.isDisplayed());

    // Verify table headers
    List<WebElement> headers = analyticsTable.findElements(By.tagName("th"));
    assertTrue(headers.size() >= 4); // At least: Food Item, Image, Views, Orders

    // Verify data rows exist
    List<WebElement> rows = analyticsTable.findElements(By.cssSelector("tbody tr"));
    assertTrue(rows.size() > 0, "Should have at least one food item");

    // Verify totals are displayed
    assertTrue(
        driver.getPageSource().contains("Total Views"),
        "Should display total views");
    assertTrue(
        driver.getPageSource().contains("Total Orders"),
        "Should display total orders");
  }

  @Test
  @DisplayName("Admin should be able to sort analytics by views")
  void testSortAnalyticsByViews() {
    // Given - Admin is on analytics dashboard
    loginAsAdmin();
    driver.get(baseUrl + "/admin/analytics");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("analytics-table")));

    // When - Click "Sort by Views" button
    WebElement sortByViewsButton = driver.findElement(By.id("sort-views-desc"));
    sortByViewsButton.click();

    // Then - Verify URL parameter changed
    wait.until(ExpectedConditions.urlContains("sort=views_desc"));
    assertTrue(driver.getCurrentUrl().contains("sort=views_desc"));

    // Verify active sort indicator
    WebElement activeButton = driver.findElement(By.cssSelector(".btn-primary"));
    assertTrue(activeButton.getText().contains("Views"));
  }

  @Test
  @DisplayName("Admin should be able to sort analytics by orders")
  void testSortAnalyticsByOrders() {
    // Given - Admin is on analytics dashboard
    loginAsAdmin();
    driver.get(baseUrl + "/admin/analytics");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("analytics-table")));

    // When - Click "Sort by Orders" button
    WebElement sortByOrdersButton = driver.findElement(By.id("sort-orders-desc"));
    sortByOrdersButton.click();

    // Then - Verify URL parameter changed
    wait.until(ExpectedConditions.urlContains("sort=orders_desc"));
    assertTrue(driver.getCurrentUrl().contains("sort=orders_desc"));

    // Verify active sort indicator
    WebElement activeButton = driver.findElement(By.cssSelector(".btn-primary"));
    assertTrue(activeButton.getText().contains("Orders"));
  }

  @Test
  @DisplayName("Non-admin user should not access analytics dashboard")
  void testNonAdminCannotAccessDashboard() {
    // When - Unauthenticated user tries to access dashboard
    driver.get(baseUrl + "/admin/analytics");

    // Then - Should be redirected to login or show 403 error
    assertTrue(
        driver.getCurrentUrl().contains("login") || driver.getPageSource().contains("403"),
        "Should redirect to login or show 403 error");
  }

  @Test
  @DisplayName("Analytics dashboard should show food item images")
  void testDashboardShowsImages() {
    // Given - Admin is on analytics dashboard
    loginAsAdmin();
    driver.get(baseUrl + "/admin/analytics");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("analytics-table")));

    // When - Check for images in table
    List<WebElement> images = driver.findElements(By.cssSelector(".analytics-table img"));

    // Then - Verify images exist
    assertTrue(images.size() > 0, "Should display food item images");
    images.forEach(
        img -> assertTrue(img.getAttribute("src") != null, "Image should have src attribute"));
  }

  /**
   * Helper method to login as admin user.
   *
   * <p>Note: Actual OAuth2 login flow is complex, this is a simplified simulation. In real E2E
   * tests, you would mock OAuth2 or use test credentials.
   */
  private void loginAsAdmin() {
    // Navigate to login page
    driver.get(baseUrl + "/login");

    // Simulate admin login (implementation depends on your OAuth2 setup)
    // For test environment, you might have a test admin user or mock OAuth2
    // This is a placeholder - adjust based on your actual authentication flow

    // Wait for redirect after login
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.urlContains(baseUrl));
  }
}
