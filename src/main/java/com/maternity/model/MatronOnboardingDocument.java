package com.maternity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matron_onboarding_documents")
public class MatronOnboardingDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private MatronOnboardingApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatronOnboardingDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatronOnboardingDocumentVisibility visibility;

    @Column(nullable = false, length = 1000)
    private String storagePath;

    @Column(nullable = false)
    private String originalFilename;

    private String mimeType;

    private Long fileSize;

    private Integer sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public MatronOnboardingDocument() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatronOnboardingApplication getApplication() {
        return application;
    }

    public void setApplication(MatronOnboardingApplication application) {
        this.application = application;
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

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

