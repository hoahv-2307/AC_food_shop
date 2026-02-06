package com.foodshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * FoodItem entity representing products available for purchase.
 *
 * <p>Contains pricing, images, category association, and cached rating data.
 */
@Entity
@Table(name = "food_items")
public class FoodItem implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @NotNull
  @DecimalMin(value = "0.00", inclusive = true)
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Size(max = 512)
  @Column(name = "image_url")
  private String imageUrl;

  @Size(max = 512)
  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false)
  private Boolean available = true;

  @Min(0)
  @Max(5)
  @Column(name = "avg_rating", precision = 3, scale = 2)
  private BigDecimal avgRating = BigDecimal.ZERO;

  @Min(0)
  @Column(name = "rating_count")
  private Integer ratingCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToOne(mappedBy = "foodItem", fetch = FetchType.LAZY)
  private FoodAnalytics analytics;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Boolean getAvailable() {
    return available;
  }

  public void setAvailable(Boolean available) {
    this.available = available;
  }

  public BigDecimal getAvgRating() {
    return avgRating;
  }

  public void setAvgRating(BigDecimal avgRating) {
    this.avgRating = avgRating;
  }

  public Integer getRatingCount() {
    return ratingCount;
  }

  public void setRatingCount(Integer ratingCount) {
    this.ratingCount = ratingCount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public FoodAnalytics getAnalytics() {
    return analytics;
  }

  public void setAnalytics(FoodAnalytics analytics) {
    this.analytics = analytics;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FoodItem)) {
      return false;
    }
    FoodItem foodItem = (FoodItem) o;
    return Objects.equals(id, foodItem.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "FoodItem{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", price=" + price
        + ", available=" + available
        + ", avgRating=" + avgRating
        + '}';
  }
}
