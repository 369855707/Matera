package com.maternity.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matron_profiles")
public class MatronProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Integer yearsOfExperience;

    @Column(nullable = false)
    private Double pricePerMonth;

    @Column(nullable = false)
    private String location;

    @Column(length = 1000)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "matron_skills", joinColumns = @JoinColumn(name = "matron_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "matron_certifications", joinColumns = @JoinColumn(name = "matron_id"))
    @Column(name = "certification")
    private List<String> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "matronProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperience> workHistory = new ArrayList<>();

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    private LocalDate availableFrom;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public MatronProfile() {
    }

    public MatronProfile(Long id, User user, Integer age, Integer yearsOfExperience, Double pricePerMonth,
                        String location, String bio, List<String> skills, List<String> certifications,
                        List<WorkExperience> workHistory, Double rating, Integer reviewCount,
                        Boolean isAvailable, LocalDate availableFrom, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.age = age;
        this.yearsOfExperience = yearsOfExperience;
        this.pricePerMonth = pricePerMonth;
        this.location = location;
        this.bio = bio;
        this.skills = skills;
        this.certifications = certifications;
        this.workHistory = workHistory;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.isAvailable = isAvailable;
        this.availableFrom = availableFrom;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Double getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(Double pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public List<WorkExperience> getWorkHistory() {
        return workHistory;
    }

    public void setWorkHistory(List<WorkExperience> workHistory) {
        this.workHistory = workHistory;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public LocalDate getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalDate availableFrom) {
        this.availableFrom = availableFrom;
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
