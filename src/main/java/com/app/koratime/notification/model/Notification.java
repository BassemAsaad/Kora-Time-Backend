package com.app.koratime.notification.model;

import com.app.koratime.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "NOTIFICATIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR2(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "BODY", length = 1000)
    private String body;

    @Column(name = "REFERENCE_ID", columnDefinition = "VARCHAR2(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID referenceId;

    @Column(name = "REFERENCE_TYPE", length = 50)
    private String referenceType;

    @Column(name = "IS_READ", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
