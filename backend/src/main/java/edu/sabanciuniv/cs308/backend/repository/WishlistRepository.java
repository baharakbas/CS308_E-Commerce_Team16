package edu.sabanciuniv.cs308.backend.repository;

import edu.sabanciuniv.cs308.backend.entity.WishlistEntity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WishlistRepository extends MongoRepository<WishlistEntity, String> {
    Optional<WishlistEntity> findByUserId(String userId);
}

