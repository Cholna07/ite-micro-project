package kh.edu.istad.platform.restapi.service;

import kh.edu.istad.platform.iam.domain.audit.AuditAction;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.ProfilePictureStorage;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class ManageCurrentProfilePictureUseCase {
    private static final String MANAGED_PREFIX = "profile-picture:";
    private final UserRepository users;
    private final ProfilePictureStorage storage;
    private final ProfilePictureImageValidator validator;
    private final CurrentActorProvider actorProvider;
    private final RecordAccountActivityEventUseCase activity;

    public ManageCurrentProfilePictureUseCase(UserRepository users, ProfilePictureStorage storage,
            ProfilePictureImageValidator validator, CurrentActorProvider actorProvider,
            RecordAccountActivityEventUseCase activity) {
        this.users = users;
        this.storage = storage;
        this.validator = validator;
        this.actorProvider = actorProvider;
        this.activity = activity;
    }

    @Transactional
    @AuditAction(action = "PROFILE_PICTURE_UPDATED", targetType = "USER", targetArgumentIndex = 0)
    public UserResult upload(UUID userUuid, byte[] content, String mediaType) {
        ProfilePictureImageValidator.ImageFormat format = validator.validate(content, mediaType);
        User user = eligibleUser(userUuid);
        String previousKey = user.getPictureUrl();
        String newKey = storage.store(content, format.extension());
        afterCompletion(() -> storage.delete(newKey), () -> deleteManaged(previousKey));
        user.setPictureUrl(newKey);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(actorProvider.getCurrentActor());
        User saved = users.save(user);
        activity.execute(saved.getUuid(), AccountActivityEventType.PROFILE_UPDATED, "Profile picture updated");
        return result(saved);
    }

    @Transactional
    @AuditAction(action = "PROFILE_PICTURE_REMOVED", targetType = "USER", targetArgumentIndex = 0)
    public UserResult remove(UUID userUuid) {
        User user = eligibleUser(userUuid);
        String previousKey = user.getPictureUrl();
        if (previousKey == null) return result(user);
        user.setPictureUrl(null);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(actorProvider.getCurrentActor());
        User saved = users.save(user);
        activity.execute(saved.getUuid(), AccountActivityEventType.PROFILE_UPDATED, "Profile picture removed");
        afterCompletion(() -> { }, () -> deleteManaged(previousKey));
        return result(saved);
    }

    @Transactional(readOnly = true)
    public Optional<ProfilePictureStorage.StoredPicture> read(UUID userUuid) {
        User user = users.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Current user was not found"));
        if (user.isDeleted()) throw new ProfilePictureAccessException("User account has been deleted");
        return storage.read(user.getPictureUrl());
    }

    private User eligibleUser(UUID userUuid) {
        User user = users.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Current user was not found"));
        if (user.isDeleted()) throw new ProfilePictureAccessException("User account has been deleted");
        if (!user.isEnabled()) throw new ProfilePictureAccessException("Disabled accounts cannot change profile pictures");
        return user;
    }

    private void deleteManaged(String key) {
        if (storage.isManagedKey(key)) storage.delete(key);
    }

    private void afterCompletion(Runnable rollback, Runnable commit) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            commit.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) commit.run();
                else rollback.run();
            }
        });
    }

    private UserResult result(User user) {
        return new UserResult(user.getUuid(), user.getUsername().value(),
                user.getEmail() == null ? null : user.getEmail().value(), user.getGivenName(), user.getFamilyName(),
                user.getMiddleName(), user.getNickname(), user.getGender(),
                user.getPhoneNumber() == null ? null : user.getPhoneNumber().value(), user.isEmailVerified(),
                user.isPhoneNumberVerified(), user.isEnabled(), !user.isAccountNonLocked(), user.getCreatedAt(),
                user.getUpdatedAt(), user.getPictureUrl());
    }
}
