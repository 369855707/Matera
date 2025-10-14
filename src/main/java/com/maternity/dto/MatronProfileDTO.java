package com.maternity.dto;

import com.maternity.model.MatronProfile;
import com.maternity.model.WorkExperience;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MatronProfileDTO {
    private Long id;
    private Long userId;
    private String name;
    private String avatar;
    private Integer age;
    private Integer yearsOfExperience;
    private Double pricePerMonth;
    private String location;
    private String bio;
    private List<String> skills;
    private List<String> certifications;
    private List<WorkExperienceDTO> workHistory;
    private Double rating;
    private Integer reviewCount;
    private Boolean isAvailable;
    private LocalDate availableFrom;

    public MatronProfileDTO() {
    }

    public MatronProfileDTO(Long id, Long userId, String name, String avatar, Integer age, Integer yearsOfExperience, Double pricePerMonth, String location, String bio, List<String> skills, List<String> certifications, List<WorkExperienceDTO> workHistory, Double rating, Integer reviewCount, Boolean isAvailable, LocalDate availableFrom) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.avatar = avatar;
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public List<WorkExperienceDTO> getWorkHistory() {
        return workHistory;
    }

    public void setWorkHistory(List<WorkExperienceDTO> workHistory) {
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

    public static MatronProfileDTO fromEntity(MatronProfile matron) {
        return new MatronProfileDTO(
                matron.getId(),
                matron.getUser().getId(),
                matron.getUser().getName(),
                matron.getUser().getAvatar(),
                matron.getAge(),
                matron.getYearsOfExperience(),
                matron.getPricePerMonth(),
                matron.getLocation(),
                matron.getBio(),
                matron.getSkills(),
                matron.getCertifications(),
                matron.getWorkHistory().stream()
                        .map(WorkExperienceDTO::fromEntity)
                        .collect(Collectors.toList()),
                matron.getRating(),
                matron.getReviewCount(),
                matron.getIsAvailable(),
                matron.getAvailableFrom()
        );
    }

    public static class WorkExperienceDTO {
        private Long id;
        private String clientName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;

        public WorkExperienceDTO() {
        }

        public WorkExperienceDTO(Long id, String clientName, LocalDate startDate, LocalDate endDate, String description) {
            this.id = id;
            this.clientName = clientName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public static WorkExperienceDTO fromEntity(WorkExperience exp) {
            return new WorkExperienceDTO(
                    exp.getId(),
                    exp.getClientName(),
                    exp.getStartDate(),
                    exp.getEndDate(),
                    exp.getDescription()
            );
        }
    }
}
