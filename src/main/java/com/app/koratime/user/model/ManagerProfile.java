package com.app.koratime.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "MANAGER_PROFILES")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR2(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "NATIONAL_ID", nullable = false, unique = true, length = 14)
    private String nationalId;

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;


}
