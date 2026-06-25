package com.app.koratime.stadium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StadiumSummary {
    private UUID id;
    private String name;
    private String city;
    private BigDecimal pricePerHour;
    private double averageRating;
    private Double distanceKM;       // null when no location provided in search
    private Set<DayOfWeek> operatingDays;
}
