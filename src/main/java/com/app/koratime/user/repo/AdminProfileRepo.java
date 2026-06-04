package com.app.koratime.user.repo;

import com.app.koratime.user.model.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminProfileRepo extends JpaRepository<AdminProfile, UUID> {

    Optional<AdminProfile> findByUserId(UUID id);
}
