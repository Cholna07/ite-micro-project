package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Address;
import kh.edu.istad.platform.iam.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Component
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;
    private final RecordAccountActivityEventUseCase recordActivity;

    public UpdateUserProfileUseCase(
            UserRepository userRepository,
            CurrentActorProvider currentActorProvider,
            RecordAccountActivityEventUseCase recordActivity
    ) {
        this.userRepository = userRepository;
        this.currentActorProvider = currentActorProvider;
        this.recordActivity = recordActivity;
    }

    @Transactional
    @AuditAction(action = "USER_UPDATED", targetType = "USER", targetArgumentIndex = 0, targetProperty = "userUuid")
    public UserResult execute(UpdateUserProfileCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Update profile command cannot be null");
        }

        User user = userRepository.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + command.userUuid()));
        String previousEmail = user.getEmail() == null ? null : user.getEmail().value();

        if (command.username() != null) {
            String username = command.username().trim();
            if (username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }
            if (!username.equals(user.getUsername().value()) && userRepository.existsByUsername(username)) {
                throw new IllegalStateException("Username is already in use");
            }
            user.setUsername(new Username(username));
        }

        if (command.email() != null) {
            String email = command.email().isBlank() ? null : command.email().trim();
            String currentEmail = user.getEmail() == null ? null : user.getEmail().value();
            if (!Objects.equals(email, currentEmail)) {
                if (email != null && userRepository.existsByEmail(email)) {
                    throw new IllegalStateException("Email is already in use");
                }
                user.setEmail(email == null ? null : new Email(email));
                user.setEmailVerified(false);
            }
        }

        if (command.givenName() != null) {
            user.setGivenName(command.givenName());
        }

        if (command.familyName() != null) {
            user.setFamilyName(command.familyName());
        }

        if (command.middleName() != null) {
            user.setMiddleName(command.middleName());
        }

        if (command.nickname() != null) {
            user.setNickname(command.nickname());
        }

        if (command.gender() != null) {
            user.setGender(command.gender());
        }

        if (command.birthdate() != null) {
            user.setBirthdate(command.birthdate());
        }

        if (command.profileUrl() != null) {
            user.setProfileUrl(command.profileUrl());
        }

        if (command.pictureUrl() != null) {
            user.setPictureUrl(command.pictureUrl());
        }

        if (command.websiteUrl() != null) {
            user.setWebsiteUrl(command.websiteUrl());
        }

        if (command.zoneInfo() != null) {
            user.setZoneInfo(command.zoneInfo());
        }

        if (command.locale() != null) {
            user.setLocale(command.locale());
        }

        if (command.phoneNumber() != null) {
            user.setPhoneNumber(new PhoneNumber(command.phoneNumber()));
        }

        if (hasAddress(command)) {
            user.setAddress(new Address(
                    command.streetAddress(),
                    command.locality(),
                    command.region(),
                    command.postalCode(),
                    command.country()
            ));
        }

        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(currentActorProvider.getCurrentActor());

        User savedUser = userRepository.save(user);
        recordActivity.execute(savedUser.getUuid(), AccountActivityEventType.PROFILE_UPDATED,
                "Profile information updated");
        String updatedEmail = savedUser.getEmail() == null ? null : savedUser.getEmail().value();
        if (!Objects.equals(previousEmail, updatedEmail)) {
            recordActivity.execute(savedUser.getUuid(), AccountActivityEventType.EMAIL_CHANGED,
                    "Email address changed");
        }

        return new UserResult(
                savedUser.getUuid(),
                savedUser.getUsername().value(),
                savedUser.getEmail() == null ? null : savedUser.getEmail().value(),
                savedUser.getGivenName(),
                savedUser.getFamilyName(),
                savedUser.getMiddleName(),
                savedUser.getNickname(),
                savedUser.getGender(),
                savedUser.getPhoneNumber() != null
                        ? savedUser.getPhoneNumber().value() : null,
                savedUser.isEmailVerified(),
                savedUser.isPhoneNumberVerified(),
                savedUser.isEnabled(),
                !savedUser.isAccountNonLocked(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getPictureUrl()
        );
    }

    private boolean hasAddress(UpdateUserProfileCommand command) {
        return command.streetAddress() != null
                || command.locality() != null
                || command.region() != null
                || command.postalCode() != null
                || command.country() != null;
    }
}
