package com.inkloom.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inkloom.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private Role role = Role.CONTENT_CREATOR;

    private String workspaceName;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String fullName, Role role) {
        this(email, password, fullName, role, null);
    }

    public RegisterRequest(String email, String password, String fullName, Role role, String workspaceName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role != null ? role : Role.CONTENT_CREATOR;
        this.workspaceName = workspaceName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role != null ? role : Role.CONTENT_CREATOR; }

    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
}
