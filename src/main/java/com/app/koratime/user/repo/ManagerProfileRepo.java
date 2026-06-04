package com.app.koratime.user.repo;

import com.app.koratime.user.model.ManagerProfile;
import com.app.koratime.user.model.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerProfileRepo extends JpaRepository<ManagerProfile, UUID> {

    Optional<PlayerProfile> findByNationalId(String nationalId);
    boolean existsByNationalId(String nationalId);

    Optional<ManagerProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
