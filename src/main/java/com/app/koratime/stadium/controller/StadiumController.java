package com.app.koratime.stadium.controller;

import com.app.koratime.common.response.ApiResponse;
import com.app.koratime.stadium.dto.StadiumRequest;
import com.app.koratime.stadium.dto.StadiumResponse;
import com.app.koratime.stadium.dto.StadiumSummary;
import com.app.koratime.stadium.service.StadiumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/stadiums")
@Tag(name = "Stadiums")
@RequiredArgsConstructor
public class StadiumController {
    private final StadiumService stadiumService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create a new stadium (manager only, must be verified)")
    public ResponseEntity<ApiResponse<StadiumResponse>> create(@Valid @RequestBody StadiumRequest stadiumRequest) {
        StadiumResponse response = stadiumService.create(stadiumRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stadium created successfully", response));
    }

    @GetMapping("/{stadiumId}")
    @Operation(summary = "Get stadium by id")
    public ResponseEntity<ApiResponse<StadiumResponse>> getById(@PathVariable String stadiumId) {
        StadiumResponse response = stadiumService.getById(UUID.fromString(stadiumId));
        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Search stadiums — all params optional")
    public ResponseEntity<ApiResponse<Page<StadiumSummary>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radiusKM,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<StadiumSummary> response = stadiumService.search(name, latitude, longitude, radiusKM, pageable);
        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PutMapping("/{stadiumId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update stadium details (owner manager only)")
    public ResponseEntity<ApiResponse<StadiumResponse>> update(
            @PathVariable String stadiumId,
            @Valid @RequestBody StadiumRequest stadiumRequest
    ) {
        StadiumResponse response = stadiumService.update(UUID.fromString(stadiumId), stadiumRequest);
        return ResponseEntity
                .ok(ApiResponse.success("Stadium updated successfully", response));
    }

    @DeleteMapping("/{stadiumId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete a stadium and all its images (owner manager only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String stadiumId) {
        stadiumService.delete(UUID.fromString(stadiumId));
        return ResponseEntity
                .ok(ApiResponse.success("Stadium deleted successfully"));
    }

    @PostMapping("/{stadiumId}/images")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Add an image to a stadium (owner manager only)")
    public ResponseEntity<ApiResponse<StadiumResponse>> addImage(
            @PathVariable String stadiumId,
            @RequestParam("image") @Valid MultipartFile file
    ) {
        StadiumResponse response = stadiumService.addImage(UUID.fromString(stadiumId), file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image uploaded successfully", response));
    }

    @DeleteMapping("/{stadiumId}/images/{imageId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete an image from a stadium")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable String stadiumId,
            @PathVariable String imageId
    ) {
        stadiumService.deleteImage(UUID.fromString(stadiumId), UUID.fromString(imageId));
        return ResponseEntity
                .ok(ApiResponse.success("Image deleted successfully"));
    }

    @PutMapping("/{stadiumId}/operating-days")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Set which days of the week the stadium is open")
    public ResponseEntity<ApiResponse<StadiumResponse>> updateOperatingDays(
            @PathVariable String stadiumId,
            @RequestBody Set<DayOfWeek> operatingDays
    ) {
        StadiumResponse response = stadiumService.updateOperatingDays(UUID.fromString(stadiumId), operatingDays);
        return ResponseEntity
                .ok(ApiResponse.success("Operating days updated successfully", response));
    }
}
