package com.maternity.service;

import com.maternity.dto.MatronOnboardingApplicationRequest;
import com.maternity.dto.MatronOnboardingApplicationResponse;
import com.maternity.dto.MatronOnboardingDocumentResponse;
import com.maternity.dto.RejectMatronOnboardingApplicationRequest;
import com.maternity.exception.ResourceNotFoundException;
import com.maternity.model.MatronOnboardingApplication;
import com.maternity.model.MatronOnboardingApplicationStatus;
import com.maternity.model.MatronOnboardingDocument;
import com.maternity.model.MatronOnboardingDocumentType;
import com.maternity.model.MatronOnboardingDocumentVisibility;
import com.maternity.model.MatronProfile;
import com.maternity.model.User;
import com.maternity.repository.MatronOnboardingApplicationRepository;
import com.maternity.repository.MatronOnboardingDocumentRepository;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MatronOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(MatronOnboardingService.class);

    private final UserRepository userRepository;
    private final MatronProfileRepository matronProfileRepository;
    private final MatronOnboardingApplicationRepository applicationRepository;
    private final MatronOnboardingDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public MatronOnboardingService(UserRepository userRepository,
                                   MatronProfileRepository matronProfileRepository,
                                   MatronOnboardingApplicationRepository applicationRepository,
                                   MatronOnboardingDocumentRepository documentRepository,
                                   FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.matronProfileRepository = matronProfileRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public MatronOnboardingApplicationResponse saveDraftOrUpdate(MatronOnboardingApplicationRequest request) {
        User user = getCurrentMatronUser();
        MatronOnboardingApplication application = getOrCreateApplication(user);

        ensureEditable(application);
        applyRequest(application, request);
        if (application.getStatus() == null) {
            application.setStatus(MatronOnboardingApplicationStatus.DRAFT);
        }
        applicationRepository.save(application);

        return toResponse(application, ResponseContext.OWNER);
    }

    @Transactional
    public MatronOnboardingApplicationResponse submit(MatronOnboardingApplicationRequest request) {
        User user = getCurrentMatronUser();
        MatronOnboardingApplication application = getOrCreateApplication(user);

        ensureEditable(application);
        applyRequest(application, request);
        validateSubmission(application);

        application.setStatus(MatronOnboardingApplicationStatus.PENDING_REVIEW);
        application.setSubmittedAt(java.time.LocalDateTime.now());
        application.setReviewNotes(null);
        application.setReviewedAt(null);
        application.setReviewedBy(null);
        applicationRepository.save(application);

        return toResponse(application, ResponseContext.OWNER);
    }

    @Transactional(readOnly = true)
    public MatronOnboardingApplicationResponse getCurrentApplication() {
        User user = getCurrentMatronUser();
        MatronOnboardingApplication application = applicationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Matron onboarding application not found"));
        return toResponse(application, ResponseContext.OWNER);
    }

    @Transactional
    public MatronOnboardingDocumentResponse uploadDocument(MatronOnboardingDocumentType documentType, MultipartFile file) {
        User user = getCurrentMatronUser();
        MatronOnboardingApplication application = getOrCreateApplication(user);
        ensureEditable(application);

        validateUpload(file, documentType);

        if (documentType == MatronOnboardingDocumentType.ID_FRONT || documentType == MatronOnboardingDocumentType.ID_BACK) {
            deleteExistingDocuments(application, documentType);
        }

        int nextSortOrder = documentRepository.findByApplicationIdOrderBySortOrderAscCreatedAtAsc(application.getId())
                .size() + 1;
        String folder = "matron-onboarding/" + user.getId() + "/" + application.getId();
        FileStorageService.StoredFile storedFile = fileStorageService.store(folder, file);

        MatronOnboardingDocument document = new MatronOnboardingDocument();
        document.setApplication(application);
        document.setDocumentType(documentType);
        document.setVisibility(resolveVisibility(documentType));
        document.setStoragePath(storedFile.relativePath());
        document.setOriginalFilename(storedFile.originalFilename());
        document.setMimeType(storedFile.contentType());
        document.setFileSize(storedFile.size());
        document.setSortOrder(nextSortOrder);

        MatronOnboardingDocument savedDocument = documentRepository.save(document);
        return toDocumentResponse(savedDocument, ResponseContext.OWNER);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadOwnDocument(Long documentId) {
        User user = getCurrentMatronUser();
        MatronOnboardingDocument document = documentRepository.findByIdAndApplicationUserId(documentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return buildFileResponse(document);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAdminDocument(Long applicationId, Long documentId) {
        MatronOnboardingDocument document = documentRepository.findByIdAndApplicationId(documentId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return buildFileResponse(document);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadPublicDocument(Long documentId) {
        MatronOnboardingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (document.getVisibility() != MatronOnboardingDocumentVisibility.PUBLIC) {
            throw new RuntimeException("Document is not public");
        }

        MatronOnboardingApplication application = document.getApplication();
        if (application.getStatus() != MatronOnboardingApplicationStatus.APPROVED) {
            throw new RuntimeException("Application is not approved");
        }

        return buildFileResponse(document);
    }

    @Transactional(readOnly = true)
    public Page<MatronOnboardingApplicationResponse> listApplications(MatronOnboardingApplicationStatus status, Pageable pageable) {
        Page<MatronOnboardingApplication> page = status == null
                ? applicationRepository.findAll(pageable)
                : applicationRepository.findByStatus(status, pageable);
        return page.map(application -> toResponse(application, ResponseContext.ADMIN));
    }

    @Transactional(readOnly = true)
    public MatronOnboardingApplicationResponse getApplicationById(Long applicationId) {
        MatronOnboardingApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Matron onboarding application not found"));
        return toResponse(application, ResponseContext.ADMIN);
    }

    @Transactional
    public MatronOnboardingApplicationResponse approve(Long applicationId) {
        MatronOnboardingApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Matron onboarding application not found"));

        if (application.getStatus() == MatronOnboardingApplicationStatus.APPROVED) {
            throw new RuntimeException("Application is already approved");
        }

        User user = application.getUser();
        MatronProfile profile = matronProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
            MatronProfile newProfile = new MatronProfile();
            newProfile.setUser(user);
            newProfile.setSkills(new ArrayList<>());
            newProfile.setCertifications(new ArrayList<>());
            newProfile.setWorkHistory(new ArrayList<>());
            return newProfile;
        });

        profile.setAge(application.getAge());
        profile.setYearsOfExperience(application.getYearsOfExperience());
        profile.setPricePerMonth(application.getMonthlyRate() != null ? application.getMonthlyRate().doubleValue() : 0.0);
        profile.setLocation(resolveLocation(application));
        profile.setBio(resolveBio(application));
        profile.setIsAvailable(true);
        profile.setAvailableFrom(null);
        matronProfileRepository.save(profile);

        String avatarUrl = resolvePrimaryAvatarUrl(application);
        if (avatarUrl != null) {
            user.setAvatar(avatarUrl);
        }
        user.setProfileCompleted(true);
        userRepository.save(user);

        application.setStatus(MatronOnboardingApplicationStatus.APPROVED);
        application.setReviewedAt(java.time.LocalDateTime.now());
        application.setReviewedBy(getCurrentAdminIdentifier());
        application.setReviewNotes(null);
        applicationRepository.save(application);

        return toResponse(application, ResponseContext.ADMIN);
    }

    @Transactional
    public MatronOnboardingApplicationResponse reject(Long applicationId, RejectMatronOnboardingApplicationRequest request) {
        MatronOnboardingApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Matron onboarding application not found"));

        if (application.getStatus() == MatronOnboardingApplicationStatus.APPROVED) {
            throw new RuntimeException("Approved application cannot be rejected");
        }

        application.setStatus(MatronOnboardingApplicationStatus.REJECTED);
        application.setReviewedAt(java.time.LocalDateTime.now());
        application.setReviewedBy(getCurrentAdminIdentifier());
        application.setReviewNotes(request.getReviewNotes());
        applicationRepository.save(application);

        return toResponse(application, ResponseContext.ADMIN);
    }

    private MatronOnboardingApplication getOrCreateApplication(User user) {
        return applicationRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    MatronOnboardingApplication application = new MatronOnboardingApplication();
                    application.setUser(user);
                    application.setStatus(MatronOnboardingApplicationStatus.DRAFT);
                    return applicationRepository.save(application);
                });
    }

    private void applyRequest(MatronOnboardingApplication application, MatronOnboardingApplicationRequest request) {
        if (request.getRealName() != null) {
            application.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            application.setPhone(request.getPhone());
        }
        if (request.getCity() != null) {
            application.setCity(request.getCity());
        }
        if (request.getServiceArea() != null) {
            application.setServiceArea(request.getServiceArea());
        }
        if (request.getAge() != null) {
            application.setAge(request.getAge());
        }
        if (request.getYearsOfExperience() != null) {
            application.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getMonthlyRate() != null) {
            application.setMonthlyRate(request.getMonthlyRate());
        }
        if (request.getServiceDescription() != null) {
            application.setServiceDescription(request.getServiceDescription());
        }
        if (request.getSelfIntro() != null) {
            application.setSelfIntro(request.getSelfIntro());
        }
    }

    private void validateSubmission(MatronOnboardingApplication application) {
        List<String> errors = new ArrayList<>();
        if (isBlank(application.getRealName())) {
            errors.add("realName is required");
        }
        if (isBlank(application.getPhone())) {
            errors.add("phone is required");
        }
        if (isBlank(application.getCity()) && isBlank(application.getServiceArea())) {
            errors.add("city or serviceArea is required");
        }
        if (application.getAge() == null) {
            errors.add("age is required");
        }
        if (application.getYearsOfExperience() == null) {
            errors.add("yearsOfExperience is required");
        }
        if (application.getMonthlyRate() == null) {
            errors.add("monthlyRate is required");
        }
        if (isBlank(application.getServiceDescription()) && isBlank(application.getSelfIntro())) {
            errors.add("serviceDescription or selfIntro is required");
        }

        List<MatronOnboardingDocument> documents = documentRepository.findByApplicationIdOrderBySortOrderAscCreatedAtAsc(application.getId());
        boolean hasProfilePhoto = documents.stream().anyMatch(document -> document.getDocumentType() == MatronOnboardingDocumentType.PROFILE_PHOTO);
        boolean hasIdFront = documents.stream().anyMatch(document -> document.getDocumentType() == MatronOnboardingDocumentType.ID_FRONT);
        boolean hasIdBack = documents.stream().anyMatch(document -> document.getDocumentType() == MatronOnboardingDocumentType.ID_BACK);

        if (!hasProfilePhoto) {
            errors.add("at least one profile photo is required");
        }
        if (!hasIdFront) {
            errors.add("ID front image is required");
        }
        if (!hasIdBack) {
            errors.add("ID back image is required");
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join("; ", errors));
        }
    }

    private void validateUpload(MultipartFile file, MatronOnboardingDocumentType documentType) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Content type is required");
        }

        boolean imageType = contentType.startsWith("image/");
        boolean pdfType = "application/pdf".equalsIgnoreCase(contentType);

        if (documentType == MatronOnboardingDocumentType.CERTIFICATE) {
            if (!imageType && !pdfType) {
                throw new RuntimeException("Certificate must be an image or PDF");
            }
        } else if (!imageType) {
            throw new RuntimeException("Document must be an image");
        }

        long maxSize = 10L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size cannot exceed 10MB");
        }
    }

    private void deleteExistingDocuments(MatronOnboardingApplication application, MatronOnboardingDocumentType documentType) {
        List<MatronOnboardingDocument> existingDocuments = documentRepository.findByApplicationIdAndDocumentType(application.getId(), documentType);
        for (MatronOnboardingDocument document : existingDocuments) {
            fileStorageService.delete(document.getStoragePath());
            documentRepository.delete(document);
        }
    }

    private MatronOnboardingDocumentVisibility resolveVisibility(MatronOnboardingDocumentType documentType) {
        return documentType == MatronOnboardingDocumentType.PROFILE_PHOTO
                ? MatronOnboardingDocumentVisibility.PUBLIC
                : MatronOnboardingDocumentVisibility.PRIVATE;
    }

    private MatronOnboardingApplicationResponse toResponse(MatronOnboardingApplication application, ResponseContext context) {
        MatronOnboardingApplicationResponse response = new MatronOnboardingApplicationResponse();
        response.setId(application.getId());
        response.setUserId(application.getUser().getId());
        response.setUserName(application.getUser().getName());
        response.setStatus(application.getStatus());
        response.setRealName(application.getRealName());
        response.setPhone(application.getPhone());
        response.setCity(application.getCity());
        response.setServiceArea(application.getServiceArea());
        response.setAge(application.getAge());
        response.setYearsOfExperience(application.getYearsOfExperience());
        response.setMonthlyRate(application.getMonthlyRate());
        response.setServiceDescription(application.getServiceDescription());
        response.setSelfIntro(application.getSelfIntro());
        response.setReviewNotes(application.getReviewNotes());
        response.setSubmittedAt(application.getSubmittedAt());
        response.setReviewedAt(application.getReviewedAt());
        response.setReviewedBy(application.getReviewedBy());
        response.setMatronProfileId(matronProfileRepository.findByUserId(application.getUser().getId()).map(MatronProfile::getId).orElse(null));
        response.setAvatarUrl(resolveAvatarUrl(application, context));

        List<MatronOnboardingDocumentResponse> documents = application.getDocuments().stream()
                .sorted(Comparator.comparing(MatronOnboardingDocument::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(MatronOnboardingDocument::getCreatedAt))
                .map(document -> toDocumentResponse(document, context))
                .toList();
        response.setDocuments(documents);

        return response;
    }

    private MatronOnboardingDocumentResponse toDocumentResponse(MatronOnboardingDocument document, ResponseContext context) {
        MatronOnboardingDocumentResponse response = new MatronOnboardingDocumentResponse();
        response.setId(document.getId());
        response.setDocumentType(document.getDocumentType());
        response.setVisibility(document.getVisibility());
        response.setDownloadUrl(resolveDownloadUrl(document, context));
        response.setOriginalFilename(document.getOriginalFilename());
        response.setMimeType(document.getMimeType());
        response.setFileSize(document.getFileSize());
        response.setSortOrder(document.getSortOrder());
        response.setCreatedAt(document.getCreatedAt());
        return response;
    }

    private String resolveAvatarUrl(MatronOnboardingApplication application, ResponseContext context) {
        Optional<MatronOnboardingDocument> photo = application.getDocuments().stream()
                .filter(document -> document.getDocumentType() == MatronOnboardingDocumentType.PROFILE_PHOTO)
                .filter(document -> document.getVisibility() == MatronOnboardingDocumentVisibility.PUBLIC)
                .min(Comparator.comparing(MatronOnboardingDocument::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        if (photo.isEmpty()) {
            return application.getUser().getAvatar();
        }
        return context == ResponseContext.PUBLIC
                ? "/api/public/matron-onboarding-documents/" + photo.get().getId()
                : "/api/public/matron-onboarding-documents/" + photo.get().getId();
    }

    private String resolvePrimaryAvatarUrl(MatronOnboardingApplication application) {
        return application.getDocuments().stream()
                .filter(document -> document.getDocumentType() == MatronOnboardingDocumentType.PROFILE_PHOTO)
                .filter(document -> document.getVisibility() == MatronOnboardingDocumentVisibility.PUBLIC)
                .min(Comparator.comparing(MatronOnboardingDocument::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(document -> "/api/public/matron-onboarding-documents/" + document.getId())
                .orElse(null);
    }

    private String resolveDownloadUrl(MatronOnboardingDocument document, ResponseContext context) {
        if (document.getVisibility() == MatronOnboardingDocumentVisibility.PUBLIC) {
            return "/api/public/matron-onboarding-documents/" + document.getId();
        }
        if (context == ResponseContext.ADMIN) {
            return "/api/admin/matron-onboarding-applications/" + document.getApplication().getId() + "/documents/" + document.getId();
        }
        return "/api/matron-onboarding/me/documents/" + document.getId();
    }

    private String resolveLocation(MatronOnboardingApplication application) {
        if (!isBlank(application.getServiceArea())) {
            return application.getServiceArea();
        }
        return application.getCity();
    }

    private String resolveBio(MatronOnboardingApplication application) {
        if (!isBlank(application.getSelfIntro())) {
            return application.getSelfIntro();
        }
        return application.getServiceDescription();
    }

    private User getCurrentMatronUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .or(() -> userRepository.findByWechatOpenId(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != User.UserRole.MATRON) {
            throw new RuntimeException("User is not a matron");
        }

        return user;
    }

    private String getCurrentAdminIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private void ensureEditable(MatronOnboardingApplication application) {
        if (application.getStatus() == MatronOnboardingApplicationStatus.PENDING_REVIEW) {
            throw new RuntimeException("Application is under review");
        }
        if (application.getStatus() == MatronOnboardingApplicationStatus.APPROVED) {
            throw new RuntimeException("Application is already approved");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseEntity<Resource> buildFileResponse(MatronOnboardingDocument document) {
        try {
            Path filePath = Path.of(fileStorageService.buildAbsolutePath(document.getStoragePath())).normalize();
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Stored file not found");
            }

            Resource resource = new FileSystemResource(filePath);
            MediaType mediaType = parseMediaType(document.getMimeType());

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read uploaded file", ex);
        }
    }

    private MediaType parseMediaType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private enum ResponseContext {
        OWNER,
        ADMIN,
        PUBLIC
    }
}
