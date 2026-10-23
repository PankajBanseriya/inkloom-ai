package com.inkloom.repository;

import com.inkloom.model.ApprovedClaim;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovedClaimRepository extends MongoRepository<ApprovedClaim, String> {
    List<ApprovedClaim> findByBrandId(String brandId);
    List<ApprovedClaim> findByBrandIdAndStatus(String brandId, String status);
}
