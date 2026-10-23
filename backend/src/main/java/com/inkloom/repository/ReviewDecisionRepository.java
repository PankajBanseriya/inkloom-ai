package com.inkloom.repository;

import com.inkloom.model.ReviewDecision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewDecisionRepository extends MongoRepository<ReviewDecision, String> {
    List<ReviewDecision> findByReviewIdOrderByTimestampDesc(String reviewId);
}
