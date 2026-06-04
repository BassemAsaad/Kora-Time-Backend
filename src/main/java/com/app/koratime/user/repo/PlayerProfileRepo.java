package com.app.koratime.user.repo;

import com.app.koratime.user.model.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerProfileRepo extends JpaRepository<PlayerProfile, UUID> {

    Optional<PlayerProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
