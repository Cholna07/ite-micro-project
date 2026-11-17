package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.ChangePasswordCommand;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordActivityTest {
    @Mock UserRepository users;
    @Mock PasswordHasher passwordHasher;
    @Mock CurrentActorProvider actor;
    @Mock RecordAccountActivityEventUseCase activity;
    @Mock PasswordPolicy passwordPolicy;

    @Test
    void recordsOnlyAfterPasswordChangeIsSaved() {
        UUID userUuid = UUID.randomUUID();
        User user = user(userUuid);
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("old", "old-hash")).thenReturn(true);
        when(passwordHasher.hash("new")).thenReturn("new-hash");
        when(actor.getCurrentActor()).thenReturn(userUuid.toString());
        when(users.save(user)).thenReturn(user);

        new ChangePasswordUseCase(users, passwordHasher, actor, activity, passwordPolicy)
                .execute(new ChangePasswordCommand(userUuid, "old", "new"));

        verify(passwordPolicy).validate("new");
        verify(passwordHasher).matches("old", "old-hash");
        verify(passwordHasher).hash("new");
        assertEquals("new-hash", user.getPasswordHash());
        verify(users).save(user);
        verify(activity).execute(userUuid, AccountActivityEventType.PASSWORD_CHANGED, "Password changed");
    }

    @Test
    void failedPasswordCheckDoesNotRecordSuccessfulChange() {
        UUID userUuid = UUID.randomUUID();
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user(userUuid)));
        when(passwordHasher.matches("wrong", "old-hash")).thenReturn(false);

        assertThrows(CurrentPasswordIncorrectException.class, () -> new ChangePasswordUseCase(users, passwordHasher, actor, activity, passwordPolicy)
                .execute(new ChangePasswordCommand(userUuid, "wrong", "new")));

        verify(passwordHasher, never()).hash(anyString());
        verify(users, never()).save(any());
        verifyNoInteractions(activity);
    }

    @Test
    void weakNewPasswordDoesNotReadOrUpdateTheAccount() {
        UUID userUuid = UUID.randomUUID();
        doThrow(new kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicyViolationException(
                12, 100, false, "Password must contain at least 12 characters"))
                .when(passwordPolicy).validate("short");

        assertThrows(kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicyViolationException.class,
                () -> new ChangePasswordUseCase(users, passwordHasher, actor, activity, passwordPolicy)
                        .execute(new ChangePasswordCommand(userUuid, "old", "short")));

        verifyNoInteractions(users, passwordHasher, actor, activity);
    }

    private User user(UUID uuid) {
        User user = new User();
        user.setUuid(uuid);
        user.setUsername(new Username("user"));
        user.setPasswordHash("old-hash");
        return user;
    }
}
