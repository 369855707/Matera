package com.maternity.repository;

import com.maternity.model.MatronOnboardingDocument;
import com.maternity.model.MatronOnboardingDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatronOnboardingDocumentRepository extends JpaRepository<MatronOnboardingDocument, Long> {
    List<MatronOnboardingDocument> findByApplicationIdOrderBySortOrderAscCreatedAtAsc(Long applicationId);
    Optional<MatronOnboardingDocument> findByIdAndApplicationId(Long id, Long applicationId);
    Optional<MatronOnboardingDocument> findByIdAndApplicationUserId(Long id, Long userId);
    List<MatronOnboardingDocument> findByApplicationIdAndDocumentType(Long applicationId, MatronOnboardingDocumentType documentType);
}

