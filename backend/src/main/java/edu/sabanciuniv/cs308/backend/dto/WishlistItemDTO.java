package edu.sabanciuniv.cs308.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Data;

@Data
public class WishlistItemDTO {
    private String productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private String mainImageUrl;
    private List<String> imageUrls;
    private Instant addedAt;
}

