package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_events")
public class AuditEvent {

    @Id
    private String id;

    @Indexed
    private String brandId;

    private String userId;
    private String eventType;
    private String resourceType;
    private String resourceId;
    private String description;
    private Instant timestamp = Instant.now();

    public AuditEvent() {}

    public AuditEvent(String brandId, String userId, String eventType, String resourceType, String resourceId, String description) {
        this.brandId = brandId;
        this.userId = userId;
        this.eventType = eventType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.description = description;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
