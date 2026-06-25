package com.app.koratime.stadium.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "STADIUM_IMAGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StadiumImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR2(36)", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STADIUM_ID", nullable = false)
    private Stadium stadium;

    @Column(name = "IMAGE_URL", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "PUBLIC_ID", nullable = false, length = 200)
    private String publicId;

    @CreationTimestamp
    @Column(name = "UPLOADED_AT", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StadiumImage other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
