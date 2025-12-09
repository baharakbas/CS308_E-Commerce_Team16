package edu.sabanciuniv.cs308.backend.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "wishlists")
public class WishlistEntity {

    @Id
    private String id;

    @Indexed
    private String userId;

    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {
        private String productId;
        private String sku;
        private Instant addedAt = Instant.now();
    }
}

