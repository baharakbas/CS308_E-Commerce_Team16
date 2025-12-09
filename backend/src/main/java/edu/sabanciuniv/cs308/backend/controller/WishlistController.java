package edu.sabanciuniv.cs308.backend.controller;

import edu.sabanciuniv.cs308.backend.dto.WishlistItemDTO;
import edu.sabanciuniv.cs308.backend.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // GET /api/wishlist  -> authenticated user'ın wishlist'i
    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> list(Authentication auth) {
        List<WishlistItemDTO> items = wishlistService.listWishlist(auth);
        return ResponseEntity.ok(items);
    }

    // POST /api/wishlist/{productId} -> ürünü wishlist'e ekle
    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(@PathVariable String productId,
                                    Authentication auth) {
        wishlistService.addToWishlist(auth, productId);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/wishlist/{productId} -> ürünü wishlist'ten çıkar
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable String productId,
                                       Authentication auth) {
        wishlistService.removeFromWishlist(auth, productId);
        return ResponseEntity.noContent().build();
    }
}
