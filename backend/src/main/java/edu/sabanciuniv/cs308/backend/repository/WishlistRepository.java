package edu.sabanciuniv.cs308.backend.repository;

import edu.sabanciuniv.cs308.backend.entity.WishlistItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WishlistRepository extends MongoRepository<WishlistItemEntity, String> {

    List<WishlistItemEntity> findByUserId(String userId);

    boolean existsByUserIdAndProductId(String userId, String productId);

    void deleteByUserIdAndProductId(String userId, String productId);
}
