package com.app.koratime.stadium.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Set;

public record StadiumRequest(
        @NotBlank(message = "Stadium name is required")
        @Size(max = 50)
        String name,

        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description,

        @NotNull(message = "Price per hour is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal pricePerHour,

        @NotNull(message = "Opening hour is required")
        @Min(value = 0, message = "Opening hour must be 0–23")
        @Max(value = 23, message = "Opening hour must be 0–23")
        Integer openingHour,

        @NotNull(message = "Closing hour is required")
        @Min(value = 1, message = "Closing hour must be 1–24")
        @Max(value = 24, message = "Closing hour must be 1–24")
        Integer closingHour,

        @Size(max = 11, message = "Phone number is invalid")
        String phone,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        Double longitude,

        @Size(max = 500)
        String address,

        @Size(max = 100)
        String city,

        Set<DayOfWeek> operatingDays,

        @Min(value = 0, message = "Cancellation hours must be between 0 and 48")
        @Max(value = 48, message = "Cancellation hours must be between 0 and 48")
        int cancellationWindowHours,

        boolean cancellationPenaltyEnabled
) {
}
