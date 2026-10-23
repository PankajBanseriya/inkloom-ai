package com.inkloom.repository;

import com.inkloom.model.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findByBrandIdOrderByTimestampDesc(String brandId);
}
