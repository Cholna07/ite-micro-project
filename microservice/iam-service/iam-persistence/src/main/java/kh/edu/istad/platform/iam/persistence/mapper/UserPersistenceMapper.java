package kh.edu.istad.platform.iam.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.valueobject.Address;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User();

        user.setId(entity.getId());
        user.setUuid(entity.getUuid());
        user.setUsername(entity.getUsername() == null
                ? null : new Username(entity.getUsername()));
        user.setEmail(entity.getEmail() == null
                ? null : new Email(entity.getEmail()));
        user.setEmailVerified(entity.isEmailVerified());
        user.setPasswordHash(entity.getPasswordHash());

        user.setGivenName(entity.getGivenName());
        user.setFamilyName(entity.getFamilyName());
        user.setMiddleName(entity.getMiddleName());
        user.setNickname(entity.getNickname());
        user.setGender(entity.getGender());
        user.setBirthdate(entity.getBirthdate());
        user.setProfileUrl(entity.getProfileUrl());
        user.setPictureUrl(entity.getPictureUrl());
        user.setWebsiteUrl(entity.getWebsiteUrl());
        user.setZoneInfo(entity.getZoneInfo());
        user.setLocale(entity.getLocale());

        user.setPhoneNumber(entity.getPhoneNumber() == null
                ? null : new PhoneNumber(entity.getPhoneNumber()));
        user.setPhoneNumberVerified(entity.isPhoneNumberVerified());

        if (entity.getStreetAddress() != null
                || entity.getLocality() != null
                || entity.getRegion() != null
                || entity.getCountry() != null
                || entity.getPostalCode() != null) {
            user.setAddress(new Address(
                    entity.getStreetAddress(),
                    entity.getLocality(),
                    entity.getRegion(),
                    entity.getCountry(),
                    entity.getPostalCode()
            ));
        }

        user.setEnabled(entity.isEnabled());
        user.setAccountNonExpired(entity.isAccountNonExpired());
        user.setAccountNonLocked(entity.isAccountNonLocked());
        user.setCredentialsNonExpired(entity.isCredentialsNonExpired());
        user.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        user.setLastLoginAt(entity.getLastLoginAt());
        user.setPasswordChangedAt(entity.getPasswordChangedAt());

        user.setDeleted(entity.isDeleted());
        user.setDeletedAt(entity.getDeletedAt());
        user.setDeletedBy(entity.getDeletedBy());

        user.setCreatedAt(entity.getCreatedAt());
        user.setCreatedBy(entity.getCreatedBy());
        user.setUpdatedAt(entity.getUpdatedAt());
        user.setUpdatedBy(entity.getUpdatedBy());
        user.setVersion(entity.getVersion());

        return user;
    }

    public UserJpaEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        UserJpaEntity entity = new UserJpaEntity();

        entity.setId(user.getId());
        entity.setUuid(user.getUuid());
        entity.setUsername(user.getUsername() == null
                ? null : user.getUsername().value());
        entity.setEmail(user.getEmail() == null
                ? null : user.getEmail().value());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setPasswordHash(user.getPasswordHash());

        entity.setGivenName(user.getGivenName());
        entity.setFamilyName(user.getFamilyName());
        entity.setMiddleName(user.getMiddleName());
        entity.setNickname(user.getNickname());
        entity.setGender(user.getGender());
        entity.setBirthdate(user.getBirthdate());
        entity.setProfileUrl(user.getProfileUrl());
        entity.setPictureUrl(user.getPictureUrl());
        entity.setWebsiteUrl(user.getWebsiteUrl());
        entity.setZoneInfo(user.getZoneInfo());
        entity.setLocale(user.getLocale());

        entity.setPhoneNumber(user.getPhoneNumber() == null
                ? null : user.getPhoneNumber().value());
        entity.setPhoneNumberVerified(user.isPhoneNumberVerified());

        entity.setStreetAddress(
                user.getAddress() == null ? null : user.getAddress().street()
        );
        entity.setLocality(
                user.getAddress() == null ? null : user.getAddress().city()
        );
        entity.setRegion(
                user.getAddress() == null ? null : user.getAddress().province()
        );
        entity.setCountry(
                user.getAddress() == null ? null : user.getAddress().country()
        );
        entity.setPostalCode(
                user.getAddress() == null ? null : user.getAddress().postalCode()
        );

        entity.setEnabled(user.isEnabled());
        entity.setAccountNonExpired(user.isAccountNonExpired());
        entity.setAccountNonLocked(user.isAccountNonLocked());
        entity.setCredentialsNonExpired(user.isCredentialsNonExpired());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setPasswordChangedAt(user.getPasswordChangedAt());

        entity.setDeleted(user.isDeleted());
        entity.setDeletedAt(user.getDeletedAt());
        entity.setDeletedBy(user.getDeletedBy());

        entity.setCreatedAt(user.getCreatedAt());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setUpdatedBy(user.getUpdatedBy());
        entity.setVersion(user.getVersion());

        return entity;
    }
}