package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.iam.domain.entity.SessionCredentialType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_sessions")
public class UserSessionJpaEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_sessions_user"))
    private UserJpaEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 20)
    private SessionCredentialType credentialType;

    @Column(name = "authentication_type", nullable = false, length = 40)
    private String authenticationType;

    @Column(length = 40)
    private String provider;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_user_id", foreignKey = @ForeignKey(name = "fk_user_sessions_revoked_by"))
    private UserJpaEntity revokedByUser;

    @Column(name = "revocation_reason", length = 40)
    private String revocationReason;
}
