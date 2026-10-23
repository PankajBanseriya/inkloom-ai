package com.inkloom.repository;

import com.inkloom.model.ReviewComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends MongoRepository<ReviewComment, String> {
    List<ReviewComment> findByReviewIdOrderByCreatedAtAsc(String reviewId);
}
