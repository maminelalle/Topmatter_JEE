package com.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 Mo

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", e);
        }
    }

    /**
     * Enregistre un fichier image et retourne l'URL relative (ex: /uploads/xxx.jpg).
     */
    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Fichier vide ou absent");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType))
            throw new IllegalArgumentException("Type de fichier non autorisé. Utilisez JPEG, PNG, GIF ou WebP.");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("Fichier trop volumineux (max 5 Mo).");

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + ext;
        Path target = uploadRoot.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return "/uploads/" + filename;
    }

    private static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
