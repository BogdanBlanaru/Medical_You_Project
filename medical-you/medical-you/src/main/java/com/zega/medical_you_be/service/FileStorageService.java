package com.zega.medical_you_be.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:52428800}") // 50MB default
    private long maxFileSize;

    private Path rootLocation;
    private Path documentsLocation;
    private Path thumbnailsLocation;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "doc", "docx", "xls", "xlsx", "txt", "rtf"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            documentsLocation = rootLocation.resolve("documents");
            thumbnailsLocation = rootLocation.resolve("thumbnails");

            Files.createDirectories(documentsLocation);
            Files.createDirectories(thumbnailsLocation);

            log.info("File storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file, Long patientId) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        // Create patient-specific directory
        Path patientDir = documentsLocation.resolve(patientId.toString());

        try {
            Files.createDirectories(patientDir);
            Path targetLocation = patientDir.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored file: {} for patient: {}", uniqueFilename, patientId);
            return patientId + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    public byte[] loadFile(String filePath) {
        try {
            Path path = documentsLocation.resolve(filePath).normalize();
            if (!path.startsWith(documentsLocation)) {
                throw new RuntimeException("Invalid file path");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filePath, e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = documentsLocation.resolve(filePath).normalize();
            if (!path.startsWith(documentsLocation)) {
                throw new RuntimeException("Invalid file path");
            }
            Files.deleteIfExists(path);

            // Also delete thumbnail if exists
            String thumbnailPath = filePath.replace("documents", "thumbnails")
                    .replaceAll("\\.[^.]+$", "_thumb.jpg");
            Path thumbPath = thumbnailsLocation.resolve(thumbnailPath.replace(filePath.split("/")[0] + "/", ""));
            Files.deleteIfExists(thumbPath);

            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }

    public String generateThumbnail(String filePath, String mimeType) {
        if (!isImage(mimeType)) {
            return null;
        }

        try {
            Path sourcePath = documentsLocation.resolve(filePath).normalize();
            if (!Files.exists(sourcePath)) {
                return null;
            }

            BufferedImage originalImage = ImageIO.read(sourcePath.toFile());
            if (originalImage == null) {
                return null;
            }

            BufferedImage thumbnail = createThumbnail(originalImage);

            String[] parts = filePath.split("/");
            String patientId = parts[0];
            String filename = parts[1];
            String thumbnailFilename = filename.replaceAll("\\.[^.]+$", "_thumb.jpg");

            Path patientThumbDir = thumbnailsLocation.resolve(patientId);
            Files.createDirectories(patientThumbDir);

            Path thumbnailPath = patientThumbDir.resolve(thumbnailFilename);
            ImageIO.write(thumbnail, "jpg", thumbnailPath.toFile());

            log.info("Generated thumbnail: {}", thumbnailFilename);
            return patientId + "/" + thumbnailFilename;
        } catch (IOException e) {
            log.error("Failed to generate thumbnail for: {}", filePath, e);
            return null;
        }
    }

    public byte[] loadThumbnail(String thumbnailPath) {
        try {
            Path path = thumbnailsLocation.resolve(thumbnailPath).normalize();
            if (!path.startsWith(thumbnailsLocation)) {
                throw new RuntimeException("Invalid thumbnail path");
            }
            if (!Files.exists(path)) {
                return null;
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to load thumbnail: {}", thumbnailPath, e);
            return null;
        }
    }

    private BufferedImage createThumbnail(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        double aspectRatio = (double) width / height;
        int targetWidth, targetHeight;

        if (aspectRatio > 1) {
            targetWidth = THUMBNAIL_WIDTH;
            targetHeight = (int) (THUMBNAIL_WIDTH / aspectRatio);
        } else {
            targetHeight = THUMBNAIL_HEIGHT;
            targetWidth = (int) (THUMBNAIL_HEIGHT * aspectRatio);
        }

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return thumbnail;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Invalid file path");
        }

        String extension = getExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isAllowedExtension(String filename) {
        return ALLOWED_EXTENSIONS.contains(getExtension(filename));
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }
}
