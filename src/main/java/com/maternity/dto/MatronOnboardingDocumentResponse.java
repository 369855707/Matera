package com.maternity.dto;

import com.maternity.model.MatronOnboardingDocumentType;
import com.maternity.model.MatronOnboardingDocumentVisibility;

import java.time.LocalDateTime;

public class MatronOnboardingDocumentResponse {

    private Long id;
    private MatronOnboardingDocumentType documentType;
    private MatronOnboardingDocumentVisibility visibility;
    private String downloadUrl;
    private String originalFilename;
    private String mimeType;
    private Long fileSize;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatronOnboardingDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(MatronOnboardingDocumentType documentType) {
        this.documentType = documentType;
    }

    public MatronOnboardingDocumentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(MatronOnboardingDocumentVisibility visibility) {
        this.visibility = visibility;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

