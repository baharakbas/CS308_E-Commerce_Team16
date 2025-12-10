package edu.sabanciuniv.cs308.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import edu.sabanciuniv.cs308.backend.dto.WishlistDTO;
import edu.sabanciuniv.cs308.backend.entity.ProductEntity;
import edu.sabanciuniv.cs308.backend.entity.WishlistEntity;
import edu.sabanciuniv.cs308.backend.repository.ProductRepository;
import edu.sabanciuniv.cs308.backend.repository.WishlistRepository;
import edu.sabanciuniv.cs308.backend.request.AddWishlistItemRequest;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void addItemCreatesWishlistAndStoresProduct() {
        String userId = "user-1";
        ProductEntity product = new ProductEntity();
        product.setId("p1");
        product.setName("Dress");
        product.setBasePrice(BigDecimal.TEN);

        when(wishlistRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(wishlistRepository.save(any(WishlistEntity.class))).thenAnswer(inv -> {
            WishlistEntity entity = inv.getArgument(0, WishlistEntity.class);
            if (entity.getId() == null) {
                entity.setId("w1");
            }
            return entity;
        });

        AddWishlistItemRequest request = new AddWishlistItemRequest();
        request.setProductId("p1");
        WishlistDTO dto = wishlistService.addItem(userId, request);

        assertThat(dto.getId()).isEqualTo("w1");
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getProductId()).isEqualTo("p1");
        assertThat(dto.getItems().get(0).getPrice()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void removeItemThrowsWhenWishlistMissing() {
        when(wishlistRepository.findByUserId("user-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                wishlistService.removeItem("user-2", "p1", null)
        ).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Wishlist not found");
    }
}

