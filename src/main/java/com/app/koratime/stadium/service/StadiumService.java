package com.app.koratime.stadium.service;

import com.app.koratime.stadium.dto.StadiumRequest;
import com.app.koratime.stadium.dto.StadiumResponse;
import com.app.koratime.stadium.dto.StadiumSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

public interface StadiumService {
    StadiumResponse create(StadiumRequest request);

    StadiumResponse getById(UUID stadiumId);

    Page<StadiumSummary> search(String name, Double latitude, Double longitude, Double radiusKM, Pageable pageable);

    StadiumResponse update(UUID stadiumId, StadiumRequest request);

    void delete(UUID stadiumId);

    StadiumResponse addImage(UUID stadiumId, MultipartFile image);

    void deleteImage(UUID stadiumId, UUID imageId);

    StadiumResponse updateOperatingDays(UUID stadiumId, Set<DayOfWeek> days);
}
