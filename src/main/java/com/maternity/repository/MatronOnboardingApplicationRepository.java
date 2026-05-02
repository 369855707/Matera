package com.maternity.repository;

import com.maternity.model.MatronOnboardingApplication;
import com.maternity.model.MatronOnboardingApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatronOnboardingApplicationRepository extends JpaRepository<MatronOnboardingApplication, Long> {
    Optional<MatronOnboardingApplication> findByUserId(Long userId);
    Optional<MatronOnboardingApplication> findByUserIdAndStatus(Long userId, MatronOnboardingApplicationStatus status);
    Page<MatronOnboardingApplication> findByStatus(MatronOnboardingApplicationStatus status, Pageable pageable);
}

