package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_roles")
public class UserRoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long roleId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(length = 100)
    private String assignedBy;

    private Instant expiresAt;

    private Instant revokedAt;

    @Column(length = 100)
    private String revokedBy;

    @Column(length = 500)
    private String reason;
}
