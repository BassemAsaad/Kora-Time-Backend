package com.app.koratime.stadium.repo;

import com.app.koratime.stadium.model.StadiumImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StadiumImageRepo extends JpaRepository<StadiumImage, UUID> {

    long countByStadiumId(UUID stadiumId);
    Optional<StadiumImage> findByIdAndStadiumId(UUID id, UUID stadiumId);
}
