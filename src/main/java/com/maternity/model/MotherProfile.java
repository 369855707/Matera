package com.maternity.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mother_profiles")
public class MotherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private LocalDate dueDate;

    private LocalDate babyBirthDate;

    private String address;

    @Column(length = 1000)
    private String specialNeeds;

    // Optional: number of children
    private Integer numberOfChildren;

    // Optional: preferred matron characteristics
    @Column(length = 500)
    private String preferredMatronType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public MotherProfile() {
    }

    public MotherProfile(Long id, User user, LocalDate dueDate, LocalDate babyBirthDate,
                        String address, String specialNeeds, Integer numberOfChildren,
                        String preferredMatronType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.dueDate = dueDate;
        this.babyBirthDate = babyBirthDate;
        this.address = address;
        this.specialNeeds = specialNeeds;
        this.numberOfChildren = numberOfChildren;
        this.preferredMatronType = preferredMatronType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getBabyBirthDate() {
        return babyBirthDate;
    }

    public void setBabyBirthDate(LocalDate babyBirthDate) {
        this.babyBirthDate = babyBirthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSpecialNeeds() {
        return specialNeeds;
    }

    public void setSpecialNeeds(String specialNeeds) {
        this.specialNeeds = specialNeeds;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public String getPreferredMatronType() {
        return preferredMatronType;
    }

    public void setPreferredMatronType(String preferredMatronType) {
        this.preferredMatronType = preferredMatronType;
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
