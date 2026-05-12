package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() throws IOException {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void givenValidImageFile_whenStoreFile_shouldReturnUploadPath() throws IOException {
        final MockMultipartFile file =
                new MockMultipartFile("photo", "test.jpg", "image/jpeg", "data".getBytes());

        final String path = fileStorageService.storeFile(file);

        assertThat(path).startsWith("/uploads/").endsWith(".jpg");
    }

    @Test
    void givenFileWithNoExtension_whenStoreFile_shouldDefaultToBinExtension() throws IOException {
        final MockMultipartFile file =
                new MockMultipartFile("photo", "noext", "application/octet-stream", "data".getBytes());

        final String path = fileStorageService.storeFile(file);

        assertThat(path).endsWith(".bin");
    }

    @Test
    void givenValidFile_whenStoreFile_shouldCreatePhysicalFileInUploadDir() throws IOException {
        final MockMultipartFile file =
                new MockMultipartFile("photo", "image.png", "image/png", "pixels".getBytes());

        final String storedPath = fileStorageService.storeFile(file);
        final String filename = storedPath.replace("/uploads/", "");
        final Path physicalPath = fileStorageService.getUploadDir().resolve(filename);

        assertThat(Files.exists(physicalPath)).isTrue();
    }

    @Test
    void givenUploadDir_whenGetUploadDir_shouldReturnNonNullPath() {
        assertThat(fileStorageService.getUploadDir()).isNotNull();
    }
}

