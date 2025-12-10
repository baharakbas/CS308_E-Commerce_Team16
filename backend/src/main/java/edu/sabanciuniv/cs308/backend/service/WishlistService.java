package edu.sabanciuniv.cs308.backend.service;

import edu.sabanciuniv.cs308.backend.dto.WishlistDTO;
import edu.sabanciuniv.cs308.backend.dto.WishlistItemDTO;
import edu.sabanciuniv.cs308.backend.entity.ProductEntity;
import edu.sabanciuniv.cs308.backend.entity.WishlistEntity;
import edu.sabanciuniv.cs308.backend.repository.ProductRepository;
import edu.sabanciuniv.cs308.backend.repository.WishlistRepository;
import edu.sabanciuniv.cs308.backend.request.AddWishlistItemRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistDTO getWishlist(String userId) {
        WishlistEntity wishlist = wishlistRepository.findByUserId(userId).orElse(null);
        if (wishlist == null) {
            WishlistDTO dto = new WishlistDTO();
            dto.setId(null);
            dto.setItems(List.of());
            return dto;
        }
        return mapToDTO(wishlist);
    }

    public WishlistDTO addItem(String userId, AddWishlistItemRequest request) {
        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + request.getProductId()
                ));

        String sku = normalizeSku(request.getSku());
        if (sku != null && (product.getVariants() == null ||
                product.getVariants().stream().noneMatch(v -> sku.equals(v.getSku())))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Variant not found for SKU: " + sku);
        }

        WishlistEntity wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createWishlist(userId));

        ensureItemsList(wishlist);

        boolean alreadyExists = wishlist.getItems().stream()
                .anyMatch(i -> i.getProductId().equals(product.getId())
                        && equalsNullable(i.getSku(), sku));

        if (!alreadyExists) {
            WishlistEntity.Item item = new WishlistEntity.Item();
            item.setProductId(product.getId());
            item.setSku(sku);
            wishlist.getItems().add(item);
            wishlistRepository.save(wishlist);
        }

        return mapToDTO(wishlist);
    }

    public WishlistDTO removeItem(String userId, String productId, String sku) {
        WishlistEntity wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wishlist not found"
                ));

        ensureItemsList(wishlist);

        String normalizedSku = normalizeSku(sku);
        boolean removed = wishlist.getItems().removeIf(i ->
                i.getProductId().equals(productId) && equalsNullable(i.getSku(), normalizedSku)
        );

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found");
        }

        wishlistRepository.save(wishlist);
        return mapToDTO(wishlist);
    }

    private WishlistEntity createWishlist(String userId) {
        WishlistEntity wishlist = new WishlistEntity();
        wishlist.setUserId(userId);
        wishlist.setItems(new ArrayList<>());
        return wishlistRepository.save(wishlist);
    }

    private WishlistDTO mapToDTO(WishlistEntity wishlist) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(wishlist.getId());

        List<WishlistItemDTO> items = new ArrayList<>();
        if (wishlist.getItems() == null) {
            wishlist.setItems(new ArrayList<>());
        }

        for (WishlistEntity.Item item : wishlist.getItems()) {
            WishlistItemDTO itemDTO = new WishlistItemDTO();
            itemDTO.setProductId(item.getProductId());
            itemDTO.setSku(item.getSku());
            itemDTO.setAddedAt(item.getAddedAt());

            Optional<ProductEntity> productOpt = productRepository.findById(item.getProductId());
            productOpt.ifPresent(product -> populateProductDetails(itemDTO, product, item.getSku()));

            items.add(itemDTO);
        }

        dto.setItems(items);
        return dto;
    }

    private void populateProductDetails(WishlistItemDTO dto,
                                        ProductEntity product,
                                        String sku) {
        dto.setName(product.getName());
        dto.setMainImageUrl(product.getMainImageUrl());
        dto.setImageUrls(product.getImageUrls());

        BigDecimal price = product.getBasePrice();
        dto.setPrice(price);
        if (sku != null && product.getVariants() != null) {
            product.getVariants().stream()
                    .filter(v -> sku.equals(v.getSku()))
                    .findFirst()
                    .ifPresent(variant -> {
                        if (variant.getPrice() != null) {
                            dto.setPrice(variant.getPrice());
                        }
                    });
        }
    }

    private String normalizeSku(String sku) {
        return (sku == null || sku.isBlank()) ? null : sku.trim();
    }

    private boolean equalsNullable(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private void ensureItemsList(WishlistEntity wishlist) {
        if (wishlist.getItems() == null) {
            wishlist.setItems(new ArrayList<>());
        }
    }
}

