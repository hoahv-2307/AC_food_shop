package com.foodshop.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing analytics tracking data for a food item.
 * Tracks cumulative view counts and order counts with optimistic locking
 * for safe concurrent updates.
 */
@Entity
@Table(
    name = "food_analytics",
    indexes = {
        @Index(name = "idx_food_analytics_food_item", columnList = "food_item_id", unique = true),
        @Index(name = "idx_food_analytics_view_count", columnList = "view_count"),
        @Index(name = "idx_food_analytics_order_count", columnList = "order_count")
    }
)
public class FoodAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "food_item_id", nullable = false, unique = true)
    private Long foodItemId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", insertable = false, updatable = false)
    private FoodItem foodItem;
    
    @NotNull
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    
    @NotNull
    @Column(name = "order_count", nullable = false)
    private Long orderCount = 0L;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public FoodAnalytics() {
    }
    
    public FoodAnalytics(Long foodItemId) {
        this.foodItemId = foodItemId;
        this.viewCount = 0L;
        this.orderCount = 0L;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFoodItemId() {
        return foodItemId;
    }
    
    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }
    
    public FoodItem getFoodItem() {
        return foodItem;
    }
    
    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Long getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FoodAnalytics that = (FoodAnalytics) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "FoodAnalytics{" +
            "id=" + id +
            ", foodItemId=" + foodItemId +
            ", viewCount=" + viewCount +
            ", orderCount=" + orderCount +
            ", version=" + version +
            '}';
    }
}
