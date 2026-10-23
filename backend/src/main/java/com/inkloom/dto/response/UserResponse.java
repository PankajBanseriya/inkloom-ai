package com.inkloom.dto.response;

import com.inkloom.model.Role;
import com.inkloom.model.User;

import java.time.Instant;
import java.util.List;

public class UserResponse {

    private String id;
    private String email;
    private String fullName;
    private Role role;
    private List<String> workspaceIds;
    private boolean active;
    private Instant createdAt;

    public UserResponse() {}

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.workspaceIds = user.getWorkspaceIds();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<String> getWorkspaceIds() { return workspaceIds; }
    public void setWorkspaceIds(List<String> workspaceIds) { this.workspaceIds = workspaceIds; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
