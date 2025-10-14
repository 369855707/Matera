package com.maternity.service;

import com.maternity.dto.ReviewDTO;
import com.maternity.model.MatronProfile;
import com.maternity.model.Review;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MatronProfileRepository matronProfileRepository;

    public ReviewService(ReviewRepository reviewRepository, MatronProfileRepository matronProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.matronProfileRepository = matronProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByMatron(Long matronProfileId) {
        return reviewRepository.findByMatronProfileId(matronProfileId).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO createReview(Review review) {
        Review savedReview = reviewRepository.save(review);

        // Update matron's average rating
        updateMatronRating(review.getMatronProfile().getId());

        return ReviewDTO.fromEntity(savedReview);
    }

    @Transactional
    protected void updateMatronRating(Long matronProfileId) {
        List<Review> reviews = reviewRepository.findByMatronProfileId(matronProfileId);

        if (!reviews.isEmpty()) {
            Double averageRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);

            MatronProfile matronProfile = matronProfileRepository.findById(matronProfileId)
                    .orElseThrow(() -> new RuntimeException("Matron profile not found"));

            matronProfile.setRating(averageRating);
            matronProfile.setReviewCount(reviews.size());
            matronProfileRepository.save(matronProfile);
        }
    }
}
