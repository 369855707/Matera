package com.maternity.dto;

import com.maternity.model.Review;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private Long matronId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Double rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewDTO() {
    }

    public ReviewDTO(Long id, Long matronId, Long userId, String userName, String userAvatar, Double rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.matronId = matronId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatronId() {
        return matronId;
    }

    public void setMatronId(Long matronId) {
        this.matronId = matronId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static ReviewDTO fromEntity(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getMatronProfile().getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getUser().getAvatar(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
