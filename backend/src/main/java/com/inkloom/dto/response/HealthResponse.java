package com.inkloom.dto.response;

import java.time.Instant;

public class HealthResponse {

    private String status;
    private String application;
    private String version;
    private String environment;
    private Instant timestamp;
    private String database;

    public HealthResponse() {}

    public HealthResponse(String status, String application, String version, String environment, String database) {
        this.status = status;
        this.application = application;
        this.version = version;
        this.environment = environment;
        this.timestamp = Instant.now();
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
