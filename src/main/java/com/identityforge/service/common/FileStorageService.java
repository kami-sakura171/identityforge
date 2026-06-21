package com.identityforge.service.common;

import com.identityforge.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileStorageService {

    private final Path storagePath;

    public FileStorageService(@Value("${app.avatar.storage-path:public/avatars}") String storagePathStr) {
        this.storagePath = Paths.get(storagePathStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not create storage directory: " + this.storagePath);
        }
    }

    public void store(String filename, MultipartFile file) {
        try {
            Path targetLocation = resolveSafe(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {}", targetLocation);
        } catch (IOException e) {
            log.error("Failed to store file: {}", filename, e);
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }

    public byte[] load(String filename) {
        try {
            Path filePath = resolveSafe(filename);
            if (!Files.exists(filePath)) {
                throw new FileStorageException("File not found: " + filename);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to load file: " + e.getMessage());
        }
    }

    public void delete(String filename) {
        try {
            Path filePath = resolveSafe(filename);
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filename, e);
        }
    }

    public boolean exists(String filename) {
        Path filePath = resolveSafe(filename);
        return Files.exists(filePath);
    }

    private Path resolveSafe(String filename) {
        // Prevent path traversal attacks
        Path resolved = storagePath.resolve(filename).normalize();
        if (!resolved.startsWith(storagePath)) {
            throw new FileStorageException("Invalid file path: " + filename);
        }
        return resolved;
    }
}
