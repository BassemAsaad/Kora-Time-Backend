package com.app.koratime.auth.repo;

import com.app.koratime.auth.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE EmailVerificationToken t
                SET t.isUsed = TRUE
                WHERE t.user.id = :userId AND t.isUsed = FALSE
""")
    void invalidateAllTokens(@Param("userId") UUID userId);
}
