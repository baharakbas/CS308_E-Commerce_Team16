package edu.sabanciuniv.cs308.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class WishlistDTO {
    private String id;
    private List<WishlistItemDTO> items;
}

