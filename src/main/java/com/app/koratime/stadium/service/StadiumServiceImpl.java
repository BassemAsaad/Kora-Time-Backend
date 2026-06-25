package com.app.koratime.stadium.service;

import com.app.koratime.common.exception.BusinessViolatedException;
import com.app.koratime.common.exception.ResourceNotFoundException;
import com.app.koratime.common.security.SecurityUtils;
import com.app.koratime.stadium.dto.StadiumRequest;
import com.app.koratime.stadium.dto.StadiumResponse;
import com.app.koratime.stadium.dto.StadiumSummary;
import com.app.koratime.stadium.mapper.StadiumMapper;
import com.app.koratime.stadium.model.Stadium;
import com.app.koratime.stadium.model.StadiumImage;
import com.app.koratime.stadium.repo.StadiumImageRepo;
import com.app.koratime.stadium.repo.StadiumRepo;
import com.app.koratime.storage.StorageService;
import com.app.koratime.user.model.ManagerProfile;
import com.app.koratime.user.model.User;
import com.app.koratime.user.repo.ManagerProfileRepo;
import com.app.koratime.user.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StadiumServiceImpl implements  StadiumService {

    private static final int MAX_IMAGES = 3;
    private static final String STADIUM_IMAGES_FOLDER = "koratime/stadiums/";

    private final StadiumRepo stadiumRepo;
    private final StadiumImageRepo imageRepo;
    private final UserRepo userRepo;
    private final ManagerProfileRepo managerRepo;
    private final StadiumMapper stadiumMapper;
    private final StorageService storageService;

    @Transactional
    @Override
    public StadiumResponse create(StadiumRequest request) {
        validateOpeningAndClosingHours(request.openingHour(), request.closingHour());

        UUID managerId = SecurityUtils.getCurrentUserId();
        validateUserIsManagerAndVerified(managerId);

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", managerId));

        Stadium stadium = stadiumMapper.toEntity(request);
        stadium.setManager(manager);
        log.info("Mapping result: Latitude={}, Longitude={}", stadium.getLatitude(), stadium.getLongitude());

        Stadium savedStadium = stadiumRepo.save(stadium);
        log.info("Stadium created — id: {}, manager: {}", savedStadium.getId(), managerId);

        return stadiumMapper.toResponse(savedStadium);
    }


    @Override
    public StadiumResponse getById(UUID stadiumId) {
        Stadium stadium = stadiumRepo.findWithImagesById(stadiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", stadiumId));

        return stadiumMapper.toResponse(stadium);
    }

    @Override
    public Page<StadiumSummary> search(
            String name,
            Double latitude,
            Double longitude,
            Double radiusKM
            , Pageable pageable
    ) {
        Double minLatitude = null, maxLatitude = null, minLongitude = null, maxLongitude = null;
        if (latitude != null && longitude != null && radiusKM != null && radiusKM > 0) {
            double deltaLat = radiusKM / 111.0;
            double deltaLng = radiusKM / (111.0 * Math.cos(Math.toRadians(latitude)));

            minLatitude = latitude - deltaLat;
            maxLatitude = latitude + deltaLat;
            minLongitude = longitude - deltaLng;
            maxLongitude = longitude + deltaLng;
        }

        Page<Stadium> stadiums = stadiumRepo.
                search(name, minLatitude, maxLatitude, minLongitude, maxLongitude, pageable);

        return stadiums.map(stadium -> {
            StadiumSummary summary = stadiumMapper.toSummary(stadium);
            if (latitude != null && longitude != null) {
                summary.setDistanceKM(haversineKm(latitude, longitude, stadium.getLatitude(), stadium.getLongitude()));
            }
            return summary;
        });
    }


    @Transactional
    @Override
    public StadiumResponse update(UUID stadiumId, StadiumRequest request) {
        Stadium stadium = requireOwnership(stadiumId);

        validateOpeningAndClosingHours(request.openingHour(), request.closingHour());

        stadiumMapper.updateEntity(request, stadium);
        log.info("Stadium updated — id: {}", stadium.getId());

        return stadiumMapper.toResponse(stadium);
    }


    @Transactional
    @Override
    public void delete(UUID stadiumId) {
        Stadium stadium = requireOwnership(stadiumId);

        stadium.getImages().forEach(img -> storageService.delete(img.getPublicId()));

        stadiumRepo.delete(stadium);
        log.info("Stadium deleted — id: {}", stadium.getId());
    }

    @Transactional
    @Override
    public StadiumResponse addImage(UUID stadiumId, MultipartFile file) {
        Stadium stadium = requireOwnership(stadiumId);

        long imageCount = imageRepo.countByStadiumId(stadiumId);
        if (imageCount >= MAX_IMAGES) {
            throw new BusinessViolatedException(
                    "A stadium can have at most " + MAX_IMAGES + " images. Remove one first.");
        }

        StorageService.UploadResult uploadResult = storageService.upload(file,STADIUM_IMAGES_FOLDER + stadiumId);

        StadiumImage image = StadiumImage.builder()
                .stadium(stadium)
                .imageUrl(uploadResult.url())
                .publicId(uploadResult.publicId())
                .build();
        StadiumImage savedImage = imageRepo.save(image);
        log.info("Stadium image added — id: {}", savedImage.getId());

        return stadiumMapper.toResponse(stadium);
    }

    @Transactional
    @Override
    public void deleteImage(UUID stadiumId, UUID imageId) {
        requireOwnership(stadiumId);

        StadiumImage image = imageRepo.findByIdAndStadiumId(imageId, stadiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", imageId));

        storageService.delete(image.getPublicId());
        imageRepo.delete(image);

        log.info("Image {} deleted from stadium {}", imageId, stadiumId);
    }

    @Transactional
    @Override
    public StadiumResponse updateOperatingDays(UUID stadiumId, Set<DayOfWeek> days) {
        Stadium stadium = requireOwnership(stadiumId);
        stadium.setOperatingDays(days != null ? days : Set.of());

        log.info("Stadium operating days updated — id: {}", stadium.getId());

        return stadiumMapper.toResponse(stadium);
    }

    private void validateOpeningAndClosingHours(int openingHour, int closingHour) {
        if (closingHour <= openingHour) {
            throw new BusinessViolatedException("Closing hour must be after opening hour");
        }
    }

    private void validateUserIsManagerAndVerified(UUID userId) {
        ManagerProfile manager = managerRepo.findByUserId(userId)
                .orElseThrow(() -> new BusinessViolatedException("User is not a manager"));

        if (!manager.isVerified()) {
            throw new BusinessViolatedException(
                    "Your manager account is not yet verified by an admin. " +
                            "Please wait for verification before adding stadiums.");
        }
    }

    private double haversineKm(Double latitude1, Double longitude1, double latitude2, double longitude2) {
        final int R = 6371; // Earth's radius in km
        double dLatitude = Math.toRadians(latitude2 - latitude1);
        double dLongitude = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c * 100.0) / 100.0; // 2 decimal places
    }

    private Stadium requireOwnership(UUID id) {
        Stadium stadium = stadiumRepo.findWithImagesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", id));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!stadium.isOwnedBy(currentUserId)) {
            throw new AccessDeniedException("You can only modify your own stadiums");
        }
        return stadium;
    }

}
