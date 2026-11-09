package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.iam.domain.valueobject.IdentityProvider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "identities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_identities_uuid", columnNames = "uuid"),
                @UniqueConstraint(
                        name = "uk_identities_provider_subject",
                        columnNames = {"provider", "provider_subject"}
                )
        }
)
public class IdentityJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdentityProvider provider;

    @Column(nullable = false, length = 255)
    private String providerSubject;

    @Column(length = 150)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 150)
    private String displayName;

    @Column(length = 100)
    private String username;

    @Column(length = 500)
    private String pictureUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    private Instant linkedAt;

    private Instant lastLoginAt;

    @Column(nullable = false)
    private boolean isDeleted;

    private Instant deletedAt;

    @Column(length = 100)
    private String deletedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(length = 100, updatable = false)
    private String createdBy;

    @LastModifiedDate
    private Instant updatedAt;

    @LastModifiedBy
    @Column(length = 100)
    private String updatedBy;

    @Version
    private Long version;
}
