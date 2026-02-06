package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.foodshop.domain.Category;
import com.foodshop.domain.FoodItem;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.FoodItemRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for FoodItemService.
 *
 * <p>Tests verify food item retrieval operations including pagination, filtering by category,
 * search functionality, and top-rated items selection.
 */
@ExtendWith(MockitoExtension.class)
class FoodItemServiceTest {

  @Mock private FoodItemRepository foodItemRepository;

  @InjectMocks private FoodItemService foodItemService;

  private FoodItem testFoodItem;
  private Category testCategory;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Pizza");
    testCategory.setActive(true);

    testFoodItem = new FoodItem();
    testFoodItem.setId(1L);
    testFoodItem.setName("Margherita Pizza");
    testFoodItem.setDescription("Classic pizza with tomato and mozzarella");
    testFoodItem.setPrice(new BigDecimal("12.99"));
    testFoodItem.setAvailable(true);
    testFoodItem.setCategory(testCategory);
    testFoodItem.setAvgRating(BigDecimal.valueOf(4.5));
    testFoodItem.setRatingCount(127);

    pageable = PageRequest.of(0, 12);
  }

  @Test
  void findAllAvailable_shouldReturnAvailableItems() {
    // Arrange
    Page<FoodItem> expectedPage = new PageImpl<>(Arrays.asList(testFoodItem));
    when(foodItemRepository.findAllAvailable(pageable)).thenReturn(expectedPage);

    // Act
    Page<FoodItem> result = foodItemService.findAllAvailable(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(testFoodItem.getName(), result.getContent().get(0).getName());
    verify(foodItemRepository).findAllAvailable(pageable);
  }

  @Test
  void findByCategoryId_shouldReturnItemsInCategory() {
    // Arrange
    Long categoryId = 1L;
    Page<FoodItem> expectedPage = new PageImpl<>(Arrays.asList(testFoodItem));
    when(foodItemRepository.findByCategoryIdAndAvailable(categoryId, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<FoodItem> result = foodItemService.findByCategoryId(categoryId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(testFoodItem.getCategory().getId(), categoryId);
    verify(foodItemRepository).findByCategoryIdAndAvailable(categoryId, pageable);
  }

  @Test
  void search_shouldReturnMatchingItems() {
    // Arrange
    String searchTerm = "pizza";
    Page<FoodItem> expectedPage = new PageImpl<>(Arrays.asList(testFoodItem));
    when(foodItemRepository.searchByNameOrDescription(searchTerm, pageable)).thenReturn(expectedPage);

    // Act
    Page<FoodItem> result = foodItemService.search(searchTerm, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertTrue(result.getContent().get(0).getName().toLowerCase().contains("pizza"));
    verify(foodItemRepository).searchByNameOrDescription(searchTerm, pageable);
  }

  @Test
  void findTopRated_shouldReturnHighRatedItems() {
    // Arrange
    int minRatingCount = 5;
    Page<FoodItem> expectedPage = new PageImpl<>(Arrays.asList(testFoodItem));
    when(foodItemRepository.findTopRated(minRatingCount, pageable)).thenReturn(expectedPage);

    // Act
    Page<FoodItem> result = foodItemService.findTopRated(minRatingCount, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    FoodItem item = result.getContent().get(0);
    assertTrue(item.getRatingCount() >= minRatingCount);
    assertTrue(item.getAvgRating().compareTo(BigDecimal.ZERO) > 0);
    verify(foodItemRepository).findTopRated(minRatingCount, pageable);
  }

  @Test
  void findById_shouldReturnFoodItem_whenExists() {
    // Arrange
    Long id = 1L;
    when(foodItemRepository.findById(id)).thenReturn(Optional.of(testFoodItem));

    // Act
    FoodItem result = foodItemService.findById(id);

    // Assert
    assertNotNull(result);
    assertEquals(testFoodItem.getId(), result.getId());
    assertEquals(testFoodItem.getName(), result.getName());
    verify(foodItemRepository).findById(id);
  }

  @Test
  void findById_shouldThrowException_whenNotExists() {
    // Arrange
    Long id = 999L;
    when(foodItemRepository.findById(id)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> foodItemService.findById(id));

    assertEquals("Food item not found with id: 999", exception.getMessage());
    verify(foodItemRepository).findById(id);
  }

  @Test
  void findAllAvailable_shouldReturnEmptyPage_whenNoItems() {
    // Arrange
    Page<FoodItem> emptyPage = Page.empty();
    when(foodItemRepository.findAllAvailable(pageable)).thenReturn(emptyPage);

    // Act
    Page<FoodItem> result = foodItemService.findAllAvailable(pageable);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.getTotalElements());
    verify(foodItemRepository).findAllAvailable(pageable);
  }

  @Test
  void search_shouldHandleEmptySearchTerm() {
    // Arrange
    String emptySearch = "";
    Page<FoodItem> expectedPage = new PageImpl<>(Arrays.asList(testFoodItem));
    when(foodItemRepository.searchByNameOrDescription(emptySearch, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<FoodItem> result = foodItemService.search(emptySearch, pageable);

    // Assert
    assertNotNull(result);
    verify(foodItemRepository).searchByNameOrDescription(emptySearch, pageable);
  }
}
