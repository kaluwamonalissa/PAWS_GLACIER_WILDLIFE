package za.co.mwm.paws.paws.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final String UPLOADS_PATH_PREFIX = "/uploads/";

    private final Path uploadDir;

    public FileStorageService(@Value("${paws.uploads.dir:uploads}") final String uploadsDir)
            throws IOException {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public String storeFile(final MultipartFile file) throws IOException {
        final String originalFilename = file.getOriginalFilename();
        final String extension = extractExtension(originalFilename);
        final String filename = UUID.randomUUID() + extension;
        final Path targetPath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath);
        return UPLOADS_PATH_PREFIX + filename;
    }

    private String extractExtension(final String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return ".bin";
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}

