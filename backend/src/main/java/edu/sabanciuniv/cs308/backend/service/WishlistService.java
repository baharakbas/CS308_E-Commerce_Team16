package edu.sabanciuniv.cs308.backend.service;

import edu.sabanciuniv.cs308.backend.dto.WishlistItemDTO;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import edu.sabanciuniv.cs308.backend.entity.WishlistItemEntity;
import edu.sabanciuniv.cs308.backend.repository.UserRepository;
import edu.sabanciuniv.cs308.backend.repository.WishlistRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
    }

    // Authenticated user'ın id'sini bul
    private String getCurrentUserId(Authentication auth) {
        String email = auth.getName(); // projende username/email ne ise

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return user.getId(); // UserEntity içinde id field'ının ismine göre değiştir
    }

    public List<WishlistItemDTO> listWishlist(Authentication auth) {
        String userId = getCurrentUserId(auth);

        List<WishlistItemEntity> items = wishlistRepository.findByUserId(userId);

        return items.stream()
                .map(item -> new WishlistItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public void addToWishlist(Authentication auth, String productId) {
        String userId = getCurrentUserId(auth);

        // Aynı ürün zaten wishlist'te ise tekrar ekleme
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return;
        }

        WishlistItemEntity entity = new WishlistItemEntity(
                userId,
                productId,
                Instant.now()
        );

        wishlistRepository.save(entity);
    }

    public void removeFromWishlist(Authentication auth, String productId) {
        String userId = getCurrentUserId(auth);
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
