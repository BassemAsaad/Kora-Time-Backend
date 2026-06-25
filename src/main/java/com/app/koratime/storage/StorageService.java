package com.app.koratime.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Uploads a file and return the Public URL + Cloudinary public_id.
     * @param folder Cloudinary folder path, e.g. "koratime/stadiums"
     */
    UploadResult upload(MultipartFile file, String folder);

    /**
     * Permanently deletes a file from Cloudinary.
     * @param publicId the public_id returned by upload()
     */
    void delete(String publicId);

    record UploadResult(
            String url,
            String publicId
    ) {
    }
}
