package kh.edu.istad.platform.iam.domain.usecase.identity;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.IdentityProvider;
import kh.edu.istad.platform.iam.domain.valueobject.ProviderSubject;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnlinkIdentityUseCaseTest {
    @Mock UserRepository users;
    @Mock IdentityRepository identities;
    @Mock CurrentActorProvider actors;
    @Mock RecordAccountActivityEventUseCase activity;

    @Test
    void preventsUnlinkingTheOnlyUsableLoginMethod() {
        UUID userUuid = UUID.randomUUID();
        UUID identityUuid = UUID.randomUUID();
        User user = user(userUuid, null);
        Identity identity = identity(identityUuid, user.getId());
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(identities.findByUuid(identityUuid)).thenReturn(Optional.of(identity));
        when(identities.findByUserId(user.getId())).thenReturn(List.of(identity));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new UnlinkIdentityUseCase(users, identities, actors, activity).execute(userUuid, identityUuid));

        assertTrue(error.getMessage().contains("only usable login method"));
        verify(identities, never()).save(any());
        verifyNoInteractions(activity);
    }

    @Test
    void allowsUnlinkWhenPasswordLoginRemains() {
        UUID userUuid = UUID.randomUUID();
        UUID identityUuid = UUID.randomUUID();
        User user = user(userUuid, "{bcrypt}encoded-hash");
        Identity identity = identity(identityUuid, user.getId());
        when(users.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(identities.findByUuid(identityUuid)).thenReturn(Optional.of(identity));
        when(identities.findByUserId(user.getId())).thenReturn(List.of(identity));
        when(actors.getCurrentActor()).thenReturn(userUuid.toString());

        assertDoesNotThrow(() -> new UnlinkIdentityUseCase(users, identities, actors, activity).execute(userUuid, identityUuid));
        assertTrue(identity.isDeleted());
        verify(identities).save(identity);
        verify(activity).execute(userUuid, kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType.SOCIAL_ACCOUNT_UNLINKED, "A social account was unlinked");
    }

    private User user(UUID uuid, String passwordHash) {
        User user = new User();
        user.setId(12L);
        user.setUuid(uuid);
        user.setUsername(new Username("local-user"));
        user.setPasswordHash(passwordHash);
        return user;
    }

    private Identity identity(UUID uuid, Long userId) {
        Identity identity = new Identity();
        identity.setUuid(uuid);
        identity.setUserId(userId);
        identity.setProvider(IdentityProvider.GOOGLE);
        identity.setProviderSubject(new ProviderSubject("subject"));
        return identity;
    }
}
