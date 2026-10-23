package com.inkloom.repository;

import com.inkloom.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByBrandIdOrderByCreatedAtDesc(String brandId);
    List<Review> findByAuthorUserIdOrderByCreatedAtDesc(String authorUserId);
    long countByBrandId(String brandId);
    long countByBrandIdAndStatus(String brandId, String status);
}
