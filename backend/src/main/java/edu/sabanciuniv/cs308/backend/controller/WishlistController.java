package edu.sabanciuniv.cs308.backend.controller;

import edu.sabanciuniv.cs308.backend.dto.WishlistDTO;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import edu.sabanciuniv.cs308.backend.repository.UserRepository;
import edu.sabanciuniv.cs308.backend.request.AddWishlistItemRequest;
import edu.sabanciuniv.cs308.backend.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    public WishlistController(WishlistService wishlistService,
                              UserRepository userRepository) {
        this.wishlistService = wishlistService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<WishlistDTO> list(Authentication auth) {
        UserEntity user = ensureUser(auth);
        return ResponseEntity.ok(wishlistService.getWishlist(user.getId()));
    }

    @PostMapping
    public ResponseEntity<WishlistDTO> add(Authentication auth,
                                           @RequestBody AddWishlistItemRequest request) {
        UserEntity user = ensureUser(auth);
        WishlistDTO dto = wishlistService.addItem(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistDTO> remove(Authentication auth,
                                              @PathVariable String productId,
                                              @RequestParam(required = false) String sku) {
        UserEntity user = ensureUser(auth);
        WishlistDTO dto = wishlistService.removeItem(user.getId(), productId, sku);
        return ResponseEntity.ok(dto);
    }

    private UserEntity ensureUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String email = auth.getName();
        return userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));
    }
}

