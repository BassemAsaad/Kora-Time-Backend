package com.app.koratime.stadium.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record StadiumResponse(
        UUID id,
        UUID managerId,
        String managerName,
        String name,
        String description,
        BigDecimal pricePerHour,
        int openingHour,
        int closingHour,
        String phone,
        Double latitude,
        Double longitude,
        String address,
        String city,
        List<String> imageUrls,
        Set<DayOfWeek> operatingDays,
        double averageRating,
        int reviewCount,
        boolean favourite,
        int cancellationWindowHours,
        boolean cancellationPenaltyEnabled,
        LocalDateTime createdAt
) {
}
