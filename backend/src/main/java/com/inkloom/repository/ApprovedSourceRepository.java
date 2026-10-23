package com.inkloom.repository;

import com.inkloom.model.ApprovedSource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovedSourceRepository extends MongoRepository<ApprovedSource, String> {
    List<ApprovedSource> findByBrandId(String brandId);
    List<ApprovedSource> findByBrandIdAndApprovalStatus(String brandId, String approvalStatus);
}
