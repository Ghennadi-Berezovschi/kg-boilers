package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallationquote.UploadedPicture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class QuotePictureStorageService {

    private static final int MAX_FILES = 3;
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadDirectory;

    public QuotePictureStorageService(@Value("${kg.uploads.quote-pictures-dir}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public List<UploadedPicture> storePictures(List<MultipartFile> pictures) {
        List<MultipartFile> files = pictures == null
                ? List.of()
                : pictures.stream().filter(file -> file != null && !file.isEmpty()).toList();

        if (files.isEmpty()) {
            return List.of();
        }
        if (files.size() > MAX_FILES) {
            throw new IllegalArgumentException("Please upload no more than 3 pictures.");
        }

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not prepare picture upload folder.", ex);
        }

        List<UploadedPicture> uploadedPictures = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedPictures.add(storePicture(file));
        }
        return uploadedPictures;
    }

    private UploadedPicture storePicture(MultipartFile file) {
        validate(file);

        String extension = extensionFor(file);
        String storedFilename = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(storedFilename).normalize();

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save uploaded picture.", ex);
        }

        return new UploadedPicture(
                cleanOriginalFilename(file.getOriginalFilename()),
                storedFilename,
                "/uploads/quote-pictures/" + storedFilename,
                file.getContentType(),
                file.getSize()
        );
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Each picture must be 5 MB or smaller.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Please upload JPG, PNG or WebP pictures only.");
        }
    }

    private String extensionFor(MultipartFile file) {
        return switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String cleanOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "picture";
        }

        String cleaned = originalFilename
                .replace("\\", "/");
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1)
                .replaceAll("[^A-Za-z0-9._ -]", "")
                .trim();

        return cleaned.isBlank() ? "picture" : cleaned.toLowerCase(Locale.UK);
    }
}
