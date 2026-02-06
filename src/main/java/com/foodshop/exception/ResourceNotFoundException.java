package com.foodshop.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ResourceNotFoundException forEntity(String entityName, Long id) {
    return new ResourceNotFoundException(
        String.format("%s not found with id: %d", entityName, id));
  }

  public static ResourceNotFoundException forField(String entityName, String fieldName,
      Object fieldValue) {
    return new ResourceNotFoundException(
        String.format("%s not found with %s: %s", entityName, fieldName, fieldValue));
  }
}
