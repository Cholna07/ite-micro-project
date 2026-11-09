package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Address;
import kh.edu.istad.platform.iam.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final CurrentActorProvider currentActorProvider;

    public UpdateUserProfileUseCase(
            UserRepository userRepository,
            CurrentActorProvider currentActorProvider
    ) {
        this.userRepository = userRepository;
        this.currentActorProvider = currentActorProvider;
    }

    public UserResult execute(UpdateUserProfileCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Update profile command cannot be null");
        }

        User user = userRepository.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + command.userUuid()));

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

        return new UserResult(
                savedUser.getUuid(),
                savedUser.getUsername().value(),
                savedUser.getEmail().value(),
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
                savedUser.getUpdatedAt()
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