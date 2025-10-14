package com.maternity.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "work_experiences")
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matron_profile_id", nullable = false)
    private MatronProfile matronProfile;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String description;

    // Constructors
    public WorkExperience() {
    }

    public WorkExperience(Long id, MatronProfile matronProfile, String clientName, LocalDate startDate,
                          LocalDate endDate, String description) {
        this.id = id;
        this.matronProfile = matronProfile;
        this.clientName = clientName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatronProfile getMatronProfile() {
        return matronProfile;
    }

    public void setMatronProfile(MatronProfile matronProfile) {
        this.matronProfile = matronProfile;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
