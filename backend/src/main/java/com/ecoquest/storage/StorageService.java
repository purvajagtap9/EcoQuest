package com.ecoquest.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Business logic (QuestSubmissionService, etc.) depends only on this interface, never on
 * filesystem/S3/Cloudinary specifics — so swapping STORAGE_PROVIDER later doesn't touch
 * anything upstream. LocalStorageService is the only implementation for now (Phase 10);
 * S3/Cloudinary implementations can be added later behind the same interface.
 */
public interface StorageService {

    /**
     * Stores the file under the given subfolder and returns a URL path the frontend can load
     * directly (e.g. "/uploads/quest-submissions/abc123.jpg").
     */
    String store(MultipartFile file, String subfolder) throws IOException;
}
