package com.app.koratime.storage;

import com.app.koratime.common.exception.BusinessViolatedException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageService implements StorageService{

    private final Cloudinary cloudinary;
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"};

    @Override
    public UploadResult upload(MultipartFile file, String folder) {
        validate(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "transformation", "w_1200,c_limit,q_auto,f_auto"
                    ));

            String url = (String) result.get("secure_url");
            String publicId = (String)  result.get("public_id");
            log.info("Image uploaded to Cloudinary — publicId: {}", publicId);

            return new UploadResult(url, publicId);
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new BusinessViolatedException("Image upload failed. Please try again.");
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary — publicId: {}", publicId);
        } catch (IOException e) {
            /*
             * Deletion failure is logged but not thrown.
             * The DB row is already gone — a dangling Cloudinary file is
             * acceptable (we can clean up via Cloudinary console later).
             * We never want a storage failure to block a user action.
             */
            log.error("Cloudinary deletion failed for publicId {}: {}", publicId, e.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessViolatedException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessViolatedException("File size must not exceed 5 MB");
        }

        String contentType = file.getContentType();
        for (String allowed : ALLOWED_TYPES) {
            if (contentType.equals(allowed)) return;

        }
        throw new BusinessViolatedException("Only JPEG, PNG, and WebP images are accepted");
    }
}
