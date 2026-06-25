package com.app.koratime.stadium.mapper;

import com.app.koratime.stadium.dto.StadiumRequest;
import com.app.koratime.stadium.dto.StadiumResponse;
import com.app.koratime.stadium.dto.StadiumSummary;
import com.app.koratime.stadium.model.Stadium;
import com.app.koratime.stadium.model.StadiumImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StadiumMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Stadium toEntity(StadiumRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(StadiumRequest request, @MappingTarget Stadium stadium);


    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(
            target = "managerName",
            expression  = "java(stadium.getManager().getFirstName() + ' ' + stadium.getManager().getLastName())"
    )
    @Mapping(target = "imageUrls", expression  = "java(toImageUrls(stadium.getImages()))")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewCount",   constant = "0")
    @Mapping(target = "favourite",     constant = "false")
    StadiumResponse toResponse(Stadium stadium);

    @Mapping(target = "distanceKM", ignore = true)
    @Mapping(target = "averageRating", constant = "0.0")
    StadiumSummary toSummary(Stadium stadium);

    default List<String> toImageUrls(List<StadiumImage> images) {
        return images.stream()
                .map(StadiumImage::getImageUrl)
                .toList();
    }
}
