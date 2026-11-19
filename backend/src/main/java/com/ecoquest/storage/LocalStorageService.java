package com.ecoquest.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local-path}")
    private String localPath;

    @Override
    public String store(MultipartFile file, String subfolder) throws IOException {
        Path targetDir = Path.of(localPath, subfolder);
        Files.createDirectories(targetDir);

        String extension = extensionOf(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path targetFile = targetDir.resolve(filename);

        // normalize() + startsWith guard against a crafted filename attempting path traversal
        Path normalized = targetFile.normalize();
        if (!normalized.startsWith(Path.of(localPath).normalize())) {
            throw new IOException("Invalid file path");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, normalized, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + subfolder + "/" + filename;
    }

    private String extensionOf(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dot = cleaned.lastIndexOf('.');
        return dot >= 0 ? cleaned.substring(dot).toLowerCase() : "";
    }
}
