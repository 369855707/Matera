package com.maternity.dto;

import com.maternity.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private Boolean profileCompleted;

    // Mother-specific fields
    private LocalDateTime dueDate;
    private LocalDateTime babyBirthDate;
    private String address;
    private String specialNeeds;

    // Matron-specific fields
    private Integer experienceYears;
    private List<String> certificates;
    private String serviceArea;
    private String priceRange;
    private String bio;

    public UserDTO() {
    }

    public UserDTO(Long id, String name, String email, String phone, String avatar, User.UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public User.UserRole getRole() {
        return role;
    }

    public void setRole(User.UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getBabyBirthDate() {
        return babyBirthDate;
    }

    public void setBabyBirthDate(LocalDateTime babyBirthDate) {
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

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getRole(),
                user.getCreatedAt()
        );

        dto.setProfileCompleted(user.getProfileCompleted());

        // Note: Mother and Matron profile data should be loaded by the service layer
        // using MotherProfileRepository and MatronProfileRepository
        // This DTO only contains basic user data

        return dto;
    }
}
