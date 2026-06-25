package com.app.koratime.stadium.model;

import com.app.koratime.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

@Table(name = "STADIUMS")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stadium {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR2(36)", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID", nullable = false)
    private User manager;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "PRICE_PER_HOUR", nullable = false)
    private BigDecimal pricePerHour;

    @Column(name = "OPENING_HOUR", nullable = false)
    private int openingHour;

    @Column(name = "CLOSING_HOUR", nullable = false)
    private int closingHour;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, columnDefinition = "NUMBER(10,7)")
    private Double latitude;

    @Column(nullable = false, columnDefinition = "NUMBER(10,7)")
    private Double longitude;

    @Column(nullable = false, length = 150)
    private String address;

    @Column(nullable = false, length = 20)
    private String city;

    @Builder.Default
    @Column(name = "CANCELLATION_WINDOW_HOURS", nullable = false)
    private int cancellationWindowHours = 8;

    @Builder.Default
    @Column(name = "CANCELLATION_PENALTY_ENABLE", nullable = false)
    private boolean cancellationPenaltyEnable = false;

    @Builder.Default
    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StadiumImage> images = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "STADIUM_OPERATING_DAYS", joinColumns = @JoinColumn(name = "STADIUM_ID"))
    @Column(name = "OPERATING_DAY")
    private Set<DayOfWeek> operatingDays = new HashSet<>();

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isOwnedBy(UUID userId) {
        return manager.getId().equals(userId);
    }

    public boolean isOpenTo(DayOfWeek day) {
        return operatingDays.contains(day);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stadium other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
