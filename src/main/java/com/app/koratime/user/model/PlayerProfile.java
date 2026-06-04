package com.app.koratime.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "PLAYER_PROFILES")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR2(36)")
    private UUID id;

    @Builder.Default
    @Column(name = "REPUTATION_SCORE", nullable = false)
    private BigDecimal reputationScore = BigDecimal.valueOf(5.0);

    @Builder.Default
    @Column(name = "LATE_CANCELLATION_COUNT", nullable = false)
    private Integer lateCancellationCount = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

}
