package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account_activity_events")
public class AccountActivityEventJpaEntity {
    @Id
    @Column(name = "event_uuid", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", nullable = false,
            foreignKey = @ForeignKey(name = "fk_account_activity_events_user"))
    private UserJpaEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private AccountActivityEventType eventType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;
}
