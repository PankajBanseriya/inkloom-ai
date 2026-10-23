package com.inkloom.controller;

import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.HealthResponse;
import com.mongodb.client.MongoClient;
import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ClusterType;
import com.mongodb.connection.ServerDescription;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @Value("${spring.application.name:inkloom-backend}")
    private String applicationName;

    private final ObjectProvider<MongoTemplate> mongoTemplateProvider;
    private final ObjectProvider<MongoClient> mongoClientProvider;

    public HealthController(ObjectProvider<MongoTemplate> mongoTemplateProvider,
                            ObjectProvider<MongoClient> mongoClientProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
        this.mongoClientProvider = mongoClientProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        String dbStatus = checkDatabaseHealth();

        HealthResponse health = new HealthResponse(
                "UP",
                applicationName,
                "0.1.0-phase1",
                System.getProperty("spring.profiles.active", "dev"),
                dbStatus
        );

        return ResponseEntity.ok(ApiResponse.ok("Inkloom AI Backend is healthy", health));
    }

    private String checkDatabaseHealth() {
        try {
            MongoClient client = mongoClientProvider.getIfAvailable();
            if (client == null) {
                return "NOT_CONFIGURED";
            }

            ClusterDescription cluster = client.getClusterDescription();
            boolean hasHealthyServer = cluster.getServerDescriptions().stream()
                    .anyMatch(ServerDescription::isOk);

            if (!hasHealthyServer || cluster.getType() == ClusterType.UNKNOWN) {
                return "UNAVAILABLE (Cluster disconnected or unreachable)";
            }

            MongoTemplate template = mongoTemplateProvider.getIfAvailable();
            if (template != null) {
                Document ping = new Document("ping", 1);
                Document result = template.getDb().runCommand(ping);
                if (result.getDouble("ok") == 1.0) {
                    return "CONNECTED";
                }
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.warn("Database connection check failed: {}", e.getMessage());
            return "UNAVAILABLE (" + e.getClass().getSimpleName() + ")";
        }
    }
}
