package com.maternity.controller;

import com.maternity.dto.ReviewDTO;
import com.maternity.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Matron reviews and ratings")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get reviews for a matron",
               description = "Retrieve all reviews and ratings for a specific matron")
    @GetMapping("/matron/{matronId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByMatron(@PathVariable Long matronId) {
        return ResponseEntity.ok(reviewService.getReviewsByMatron(matronId));
    }
}
