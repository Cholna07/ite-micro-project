package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileUseCaseTest {
    @Mock UserRepository users;
    @Mock CurrentActorProvider actors;
    @Mock RecordAccountActivityEventUseCase activity;

    @Test
    void updatesUsernameAndEmailAndInvalidatesEmailVerification() {
        UUID userUuid = UUID.randomUUID();
        User user = user(userUuid, "old-name", "old@example.com");
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(users.existsByUsername("new-name")).thenReturn(false);
        when(users.existsByEmail("new@example.com")).thenReturn(false);
        when(actors.getCurrentActor()).thenReturn(userUuid.toString());
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new UpdateUserProfileUseCase(users, actors, activity).execute(command(userUuid, "new-name", "new@example.com"));

        assertEquals("new-name", result.username());
        assertEquals("new@example.com", result.email());
        assertFalse(user.isEmailVerified());
        verify(users).save(user);
        verify(activity).execute(userUuid, kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.PROFILE_UPDATED, "Profile information updated");
        verify(activity).execute(userUuid, kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.EMAIL_CHANGED, "Email address changed");
    }

    @Test
    void rejectsUsernameAlreadyOwnedByAnotherUser() {
        UUID userUuid = UUID.randomUUID();
        User user = user(userUuid, "old-name", "old@example.com");
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(users.existsByUsername("taken-name")).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new UpdateUserProfileUseCase(users, actors, activity).execute(command(userUuid, "taken-name", null)));

        assertEquals("Username is already in use", exception.getMessage());
        verify(users, never()).save(any());
        verifyNoInteractions(activity);
    }

    @Test
    void rejectsEmailAlreadyOwnedByAnotherUser() {
        UUID userUuid = UUID.randomUUID();
        User user = user(userUuid, "old-name", "old@example.com");
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(users.existsByEmail("taken@example.com")).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new UpdateUserProfileUseCase(users, actors, activity).execute(command(userUuid, null, "taken@example.com")));

        assertEquals("Email is already in use", exception.getMessage());
        verify(users, never()).save(any());
        verifyNoInteractions(activity);
    }

    @Test
    void allowsUnchangedUsernameAndEmailWithoutTreatingThemAsDuplicates() {
        UUID userUuid = UUID.randomUUID();
        User user = user(userUuid, "same-name", "same@example.com");
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actors.getCurrentActor()).thenReturn(userUuid.toString());

        var result = new UpdateUserProfileUseCase(users, actors, activity).execute(command(userUuid, "same-name", "same@example.com"));

        assertEquals("same-name", result.username());
        assertEquals("same@example.com", result.email());
        verify(users, never()).existsByUsername(anyString());
        verify(users, never()).existsByEmail(anyString());
        verify(activity).execute(userUuid, kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.PROFILE_UPDATED, "Profile information updated");
        verify(activity, never()).execute(eq(userUuid), eq(kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.EMAIL_CHANGED), anyString());
    }

    private User user(UUID uuid, String username, String email) {
        User user = new User();
        user.setUuid(uuid);
        user.setUsername(new Username(username));
        user.setEmail(new Email(email));
        user.setEmailVerified(true);
        return user;
    }

    private UpdateUserProfileCommand command(UUID uuid, String username, String email) {
        return new UpdateUserProfileCommand(uuid, username, email, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
