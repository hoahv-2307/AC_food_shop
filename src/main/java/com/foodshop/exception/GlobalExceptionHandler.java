package com.foodshop.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the application.
 *
 * <p>Provides centralized error handling and user-friendly error pages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles ResourceNotFoundException.
   *
   * @param ex the exception
   * @param model the model
   * @return error view
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ModelAndView handleResourceNotFound(ResourceNotFoundException ex, Model model) {
    LOGGER.error("Resource not found: {}", ex.getMessage());

    ModelAndView mav = new ModelAndView("error/404");
    mav.addObject("message", ex.getMessage());
    mav.addObject("status", HttpStatus.NOT_FOUND.value());
    return mav;
  }

  /**
   * Handles NoResourceFoundException - silently ignore favicon requests.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return null to send 404 without error page for favicon
   */
  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ModelAndView handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    
    // Silently ignore favicon.ico requests - browsers always request this
    if (requestUri != null && requestUri.endsWith("/favicon.ico")) {
      return null; // Return 404 without logging error
    }
    
    LOGGER.warn("Static resource not found: {}", requestUri);
    ModelAndView mav = new ModelAndView("error/404");
    mav.addObject("message", "The requested resource was not found.");
    mav.addObject("status", HttpStatus.NOT_FOUND.value());
    return mav;
  }

  /**
   * Handles PaymentException.
   *
   * @param ex the exception
   * @param model the model
   * @return error view
   */
  @ExceptionHandler(PaymentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ModelAndView handlePaymentException(PaymentException ex, Model model) {
    LOGGER.error("Payment error: {}", ex.getMessage());

    ModelAndView mav = new ModelAndView("error/payment-error");
    mav.addObject("message", ex.getMessage());
    mav.addObject("status", HttpStatus.BAD_REQUEST.value());
    return mav;
  }

  /**
   * Handles UnauthorizedException.
   *
   * @param ex the exception
   * @param model the model
   * @return error view
   */
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ModelAndView handleUnauthorized(UnauthorizedException ex, Model model) {
    LOGGER.error("Unauthorized access: {}", ex.getMessage());

    ModelAndView mav = new ModelAndView("error/403");
    mav.addObject("message", ex.getMessage());
    mav.addObject("status", HttpStatus.FORBIDDEN.value());
    return mav;
  }

  /**
   * Handles all other exceptions.
   *
   * @param ex the exception
   * @param model the model
   * @return error view
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ModelAndView handleGeneralException(Exception ex, Model model) {
    LOGGER.error("Unexpected error", ex);

    ModelAndView mav = new ModelAndView("error/500");
    mav.addObject("message", "An unexpected error occurred. Please try again later.");
    mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    return mav;
  }
}
