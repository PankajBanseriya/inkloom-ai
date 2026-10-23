package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReviewDecisionRequest {

    @NotBlank(message = "Action is required (APPROVE, REQUEST_CHANGES, REJECT)")
    private String action;

    private String decisionNotes;

    public ReviewDecisionRequest() {}

    public ReviewDecisionRequest(String action, String decisionNotes) {
        this.action = action;
        this.decisionNotes = decisionNotes;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }
}
