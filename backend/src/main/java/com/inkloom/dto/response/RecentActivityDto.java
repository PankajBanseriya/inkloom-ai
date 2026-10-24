package com.inkloom.dto.response;

public class RecentActivityDto {

    private String id;
    private String user;
    private String role;
    private String action;
    private String target;
    private String timestamp;

    public RecentActivityDto() {}

    public RecentActivityDto(String id, String user, String role, String action, String target, String timestamp) {
        this.id = id;
        this.user = user;
        this.role = role;
        this.action = action;
        this.target = target;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
