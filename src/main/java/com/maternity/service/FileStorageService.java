package com.maternity.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile store(String folder, MultipartFile file);

    void delete(String relativePath);

    String buildAbsolutePath(String relativePath);

    record StoredFile(String relativePath, String originalFilename, String contentType, long size) {
    }
}

