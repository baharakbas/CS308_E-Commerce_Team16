package edu.sabanciuniv.cs308.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "wishlist_items")
public class WishlistItemEntity {

    @Id
    private String id;

    // Authenticated user'ın id'si
    private String userId;

    // Wishlist'e eklenen ürünün id'si
    private String productId;

    private Instant createdAt;

    public WishlistItemEntity() {
    }

    public WishlistItemEntity(String userId, String productId, Instant createdAt) {
        this.userId = userId;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
