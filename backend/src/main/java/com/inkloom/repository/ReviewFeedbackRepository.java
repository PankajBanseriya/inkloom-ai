package com.inkloom.repository;

import com.inkloom.model.ReviewFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewFeedbackRepository extends MongoRepository<ReviewFeedback, String> {
    List<ReviewFeedback> findByBrandId(String brandId);
    long countByBrandIdAndCategory(String brandId, String category);
}
