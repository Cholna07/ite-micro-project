package kh.edu.istad.platform.iam.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_uuid", columnNames = "uuid"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String givenName;

    @Column(length = 100)
    private String familyName;

    @Column(length = 100)
    private String middleName;

    @Column(length = 100)
    private String nickname;

    @Column(length = 30)
    private String gender;

    private LocalDate birthdate;

    @Column(length = 500)
    private String profileUrl;

    @Column(length = 500)
    private String pictureUrl;

    @Column(length = 500)
    private String websiteUrl;

    @Column(length = 100)
    private String zoneInfo;

    @Column(length = 20)
    private String locale;

    @Column(length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean phoneNumberVerified;

    @Column(length = 255)
    private String streetAddress;

    @Column(length = 100)
    private String locality;

    @Column(length = 100)
    private String region;

    @Column(length = 30)
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean accountNonExpired;

    @Column(nullable = false)
    private boolean accountNonLocked;

    @Column(nullable = false)
    private boolean credentialsNonExpired;

    private int failedLoginAttempts;

    private Instant lastLoginAt;

    private Instant passwordChangedAt;

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