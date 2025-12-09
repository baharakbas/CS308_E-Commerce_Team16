package edu.sabanciuniv.cs308.backend.request;

import lombok.Data;

@Data
public class AddWishlistItemRequest {
    private String productId;
    private String sku;
}

