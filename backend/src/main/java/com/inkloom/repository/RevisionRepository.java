package com.inkloom.repository;

import com.inkloom.model.Revision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevisionRepository extends MongoRepository<Revision, String> {
    List<Revision> findByReviewIdOrderByRevisionNumberDesc(String reviewId);
}
