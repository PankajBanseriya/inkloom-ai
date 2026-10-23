package com.inkloom.repository;

import com.inkloom.model.BrandWorkspace;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandWorkspaceRepository extends MongoRepository<BrandWorkspace, String> {
    Optional<BrandWorkspace> findBySlug(String slug);
    List<BrandWorkspace> findByMemberUserIdsContaining(String userId);
    boolean existsBySlug(String slug);
}
