package com.inkloom.repository;

import com.inkloom.model.BrandGuideline;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandGuidelineRepository extends MongoRepository<BrandGuideline, String> {
    Optional<BrandGuideline> findFirstByBrandIdAndStatus(String brandId, String status);
    List<BrandGuideline> findByBrandId(String brandId);
}
