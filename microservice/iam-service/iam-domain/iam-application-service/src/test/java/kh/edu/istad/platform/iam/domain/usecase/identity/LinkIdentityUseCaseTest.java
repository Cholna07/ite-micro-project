package kh.edu.istad.platform.iam.domain.usecase.identity;

import kh.edu.istad.platform.iam.domain.dto.command.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
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
class LinkIdentityUseCaseTest {
    @Mock UserRepository users;
    @Mock IdentityRepository identities;
    @Mock RecordAccountActivityEventUseCase activity;

    @Test
    void linksProviderIdentityToTheServerSelectedUser() {
        UUID targetUuid = UUID.randomUUID();
        User target = user(targetUuid);
        when(users.findByUuid(targetUuid)).thenReturn(Optional.of(target));
        when(identities.existsByProviderAndProviderSubject("GOOGLE", "subject-1")).thenReturn(false);
        when(identities.save(any())).thenAnswer(call -> call.getArgument(0));

        var result = new LinkIdentityUseCase(users, identities, activity).execute(targetUuid,
                new FederatedIdentityCommand("google", "subject-1", "user@example.com", true,
                        "provider-name", "Provider User", null, java.util.Map.of()));

        assertEquals("GOOGLE", result.provider());
        assertEquals("user@example.com", result.email());
        verify(identities).save(argThat(identity -> identity.getUserId().equals(target.getId())));
        verify(activity).execute(targetUuid, kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.SOCIAL_ACCOUNT_LINKED, "A social account was linked");
    }

    @Test
    void rejectsProviderIdentityAlreadyOwnedByAnyLocalAccount() {
        UUID targetUuid = UUID.randomUUID();
        when(users.findByUuid(targetUuid)).thenReturn(Optional.of(user(targetUuid)));
        when(identities.existsByProviderAndProviderSubject("GOOGLE", "subject-1")).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new LinkIdentityUseCase(users, identities, activity).execute(targetUuid,
                        new FederatedIdentityCommand("google", "subject-1", "user@example.com", true,
                                "provider-name", null, null, java.util.Map.of())));

        assertEquals("This federated identity is already linked", error.getMessage());
        verify(identities, never()).save(any());
        verifyNoInteractions(activity);
    }

    private User user(UUID uuid) {
        User user = new User();
        user.setId(41L);
        user.setUuid(uuid);
        user.setUsername(new Username("local-user"));
        user.setEnabled(true);
        return user;
    }
}
