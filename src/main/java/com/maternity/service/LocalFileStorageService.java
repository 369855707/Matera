package com.maternity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDirectory;

    public LocalFileStorageService(@Value("${app.upload.base-dir:./uploads}") String baseDirectory) {
        this.baseDirectory = Paths.get(baseDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(String folder, MultipartFile file) {
        try {
            Files.createDirectories(baseDirectory);
            Path folderPath = baseDirectory.resolve(folder).normalize();
            Files.createDirectories(folderPath);

            String originalFilename = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            String storedFileName = UUID.randomUUID() + extension;
            Path target = folderPath.resolve(storedFileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target);
            }

            String relativePath = baseDirectory.relativize(target).toString().replace('\\', '/');
            return new StoredFile(relativePath, originalFilename, file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file", e);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path target = baseDirectory.resolve(relativePath).normalize();
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete stored file", e);
        }
    }

    @Override
    public String buildAbsolutePath(String relativePath) {
        return baseDirectory.resolve(relativePath).normalize().toString();
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex);
    }
}

