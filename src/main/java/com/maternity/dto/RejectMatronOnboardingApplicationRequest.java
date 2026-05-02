package com.maternity.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectMatronOnboardingApplicationRequest {

    @NotBlank(message = "Review notes are required")
    private String reviewNotes;

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}

