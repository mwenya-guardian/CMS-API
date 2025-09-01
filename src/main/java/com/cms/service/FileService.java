package com.cms.service;

import com.cms.dto.response.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Value("${file.upload.dir.main}")
    private String uploadDir;

    @Value("${file.upload.dir.private}")
    private String uploadPrivateSubDir;

    @Value("${file.upload.dir.public}")
    private String uploadPublicSubDir;

    @Value("${file.upload.max-image-size:10485760}") // default 10MB
    private long maxImageFileSize;

    @Value("${file.upload.max-video-size:52428800}") // default 50MB
    private long maxVideoFileSize;

    @Value("${file.upload.allowed-image-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedImageTypesStr;

    @Value("${file.upload.allowed-video-types:video/mp4,video/quicktime,video/x-msvideo,video/x-matroska}")
    private String allowedVideoTypesStr;

    @Value("${server.port:8080}")
    private String serverPort;

    // Public backward-compatible method
    public FileUploadResponse uploadImage(MultipartFile file, Boolean isPublic) throws IOException {
        return uploadMedia(file, MediaType.PHOTO, isPublic);
    }

    // New public method for videos
    public FileUploadResponse uploadVideo(MultipartFile file, Boolean isPublic) throws IOException {
        return uploadMedia(file, MediaType.VIDEO, isPublic);
    }

    // Internal enum for clarity
    private enum MediaType { PHOTO, VIDEO }

    // Core upload implementation
    private FileUploadResponse uploadMedia(MultipartFile file, MediaType type, Boolean isPublic) throws IOException {
        validateCommon(file);

        final boolean isPhoto = type == MediaType.PHOTO;
        // Get allowed types and max size based on media type
        List<String> allowedTypes = Arrays.stream(
                        (isPhoto ? allowedImageTypesStr : allowedVideoTypesStr).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        long maxSize = isPhoto ? maxImageFileSize : maxVideoFileSize;

        // Validate content type
        String contentType = file.getContentType() == null ? "" : file.getContentType().trim().toLowerCase();
        if (allowedTypes.stream().noneMatch(t -> t.trim().toLowerCase().equals(contentType))) {
            throw new RuntimeException("Invalid file type. Allowed types: " + String.join(", ", allowedTypes));
        }

        // Validate size
        if (file.getSize() > maxSize) {
            throw new RuntimeException(String.format("File size exceeds maximum limit of %d bytes", maxSize));
        }

        // Build destination directory: <uploadDir>/<photo|videos>/<year>
        String subDir = isPhoto ? "photo" : "videos";
        String typePath = isPublic? uploadPublicSubDir : uploadPrivateSubDir;
        String year = String.valueOf(Year.now().getValue());
        Path uploadPath = Paths.get(uploadDir, typePath, subDir, year);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename with original extension (if present)
        String originalFilename = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String fileExtension = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx >= 0) {
            fileExtension = originalFilename.substring(dotIdx);
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Construct public URL for downloaded file (you should also configure a resource handler)
        // Example URL: http://localhost:8080/api/uploads/photo/2025/<filename>
        String fileUrl = String.format("http://localhost:%s/api/uploads/%s/%s/%s/%s",
                serverPort, typePath, subDir, year, uniqueFilename);

        return new FileUploadResponse(
                fileUrl,
                originalFilename,
                file.getSize(),
                contentType
        );
    }

    // Shared validations
    private void validateCommon(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No file provided");
        }
        // optional: other common checks
    }
}
