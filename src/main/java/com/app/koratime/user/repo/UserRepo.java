package com.app.koratime.user.repo;

import com.app.koratime.user.model.User;
import com.app.koratime.user.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findAllByRole(UserRole role, Pageable pageable);

    @Query("""
            SELECT u from User u
            WHERE (:role IS NULL OR u.role = :role)
            AND (
                :search IS NULL
                OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            ORDER BY u.createdAt DESC
""")
    Page<User> search(@Param("search") String search, @Param("role") UserRole role, Pageable pageable);
}
