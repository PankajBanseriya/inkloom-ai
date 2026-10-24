package com.inkloom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ObjectProvider<MongoTemplate> mongoTemplateProvider;

    public DataInitializer(ObjectProvider<MongoTemplate> mongoTemplateProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
    }

    @Override
    public void run(String... args) {
        if (isDatabaseConnected()) {
            MongoTemplate template = mongoTemplateProvider.getIfAvailable();
            String dbName = template != null ? template.getDb().getName() : "configured database";
            log.info("MongoDB Atlas connected successfully. Using '{}' as single source of truth for Inkloom AI.", dbName);
        } else {
            log.info("MongoDB is not connected or unreachable. Application will start in offline/isolated mode.");
        }
    }

    private boolean isDatabaseConnected() {
        try {
            MongoTemplate template = mongoTemplateProvider.getIfAvailable();
            if (template != null) {
                org.bson.Document ping = template.executeCommand(new org.bson.Document("ping", 1));
                return ping != null && ping.containsKey("ok");
            }
        } catch (Exception e) {
            log.debug("MongoDB ping check failed: {}", e.getMessage());
        }
        return false;
    }
}
