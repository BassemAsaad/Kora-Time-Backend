package com.app.koratime.stadium.repo;

import com.app.koratime.stadium.model.Stadium;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StadiumRepo extends JpaRepository<Stadium, UUID> {

    @EntityGraph(attributePaths = {"images", "manager"})
    Optional<Stadium> findWithImagesById(UUID id);

    Page<Stadium> findByManagerId(UUID managerId, Pageable pageable);

    @Query("""
            SELECT s FROM Stadium s
            JOIN FETCH s.manager
            WHERE
                (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND
                (:minLat IS NULL OR s.latitude BETWEEN :minLat AND :maxLat)
            AND
                (:minLng IS NULL OR s.longitude BETWEEN :minLng AND :maxLng)
            ORDER BY s.createdAt DESC
            """)
    Page<Stadium> search(
            @Param("name")   String name,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng,
            Pageable pageable
    );
}
