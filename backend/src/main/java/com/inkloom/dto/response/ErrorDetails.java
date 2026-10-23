package com.inkloom.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private String code;
    private String details;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorDetails() {}

    public ErrorDetails(String code, String details, String path) {
        this.code = code;
        this.details = details;
        this.path = path;
    }

    public ErrorDetails(String code, String details, String path, Map<String, String> validationErrors) {
        this.code = code;
        this.details = details;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
