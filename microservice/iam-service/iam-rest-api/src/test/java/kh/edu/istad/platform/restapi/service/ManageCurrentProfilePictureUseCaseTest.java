package kh.edu.istad.platform.restapi.service;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.ProfilePictureStorage;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.RecordAccountActivityEventUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.Base64;

import javax.imageio.ImageIO;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ManageCurrentProfilePictureUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final ProfilePictureStorage storage = mock(ProfilePictureStorage.class);
    private final CurrentActorProvider actor = mock(CurrentActorProvider.class);
    private final RecordAccountActivityEventUseCase activity = mock(RecordAccountActivityEventUseCase.class);
    private final UUID ownerUuid = UUID.randomUUID();
    private final User owner = user(ownerUuid);
    private ManageCurrentProfilePictureUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageCurrentProfilePictureUseCase(users, storage, new ProfilePictureImageValidator(), actor, activity);
        when(users.findByUuid(ownerUuid)).thenReturn(Optional.of(owner));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actor.getCurrentActor()).thenReturn(ownerUuid.toString());
        when(storage.store(any(), anyString())).thenReturn("profile-picture:11111111-1111-1111-1111-111111111111.png")
                .thenReturn("profile-picture:22222222-2222-2222-2222-222222222222.png");
        when(storage.isManagedKey(anyString())).thenReturn(true);
    }

    @Test
    void uploadsAndReplacesPictureForTheResolvedUserAndCleansUpOnlyAfterReplacement() throws Exception {
        String first = useCase.upload(ownerUuid, png(), "image/png").pictureUrl();
        assertEquals("profile-picture:11111111-1111-1111-1111-111111111111.png", first);
        assertEquals(ownerUuid.toString(), owner.getUpdatedBy());
        verify(activity).execute(ownerUuid, AccountActivityEventType.PROFILE_UPDATED, "Profile picture updated");

        String second = useCase.upload(ownerUuid, png(), "image/png").pictureUrl();
        assertEquals("profile-picture:22222222-2222-2222-2222-222222222222.png", second);
        verify(storage).delete(first);
        assertEquals(second, owner.getPictureUrl());
    }

    @Test
    void acceptsJpegPngAndWebpAtTheUploadUseCase() throws Exception {
        byte[] jpeg = image("jpeg");
        byte[] png = image("png");
        byte[] webp = Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEALmk0mk0iIiIiIgBoSygABc6zbAAA");
        when(storage.store(any(), anyString()))
                .thenReturn("profile-picture:11111111-1111-1111-1111-111111111111.jpg")
                .thenReturn("profile-picture:22222222-2222-2222-2222-222222222222.png")
                .thenReturn("profile-picture:33333333-3333-3333-3333-333333333333.webp");

        useCase.upload(ownerUuid, jpeg, "image/jpeg");
        useCase.upload(ownerUuid, png, "image/png");
        useCase.upload(ownerUuid, webp, "image/webp");

        verify(storage).store(jpeg, "jpg");
        verify(storage).store(png, "png");
        verify(storage).store(webp, "webp");
        assertEquals("profile-picture:33333333-3333-3333-3333-333333333333.webp", owner.getPictureUrl());
    }

    @Test
    void removesCurrentPictureAndDeletesItsManagedObject() {
        owner.setPictureUrl("profile-picture:11111111-1111-1111-1111-111111111111.png");
        assertNull(useCase.remove(ownerUuid).pictureUrl());
        verify(users).save(owner);
        verify(storage).delete("profile-picture:11111111-1111-1111-1111-111111111111.png");
        verify(activity).execute(ownerUuid, AccountActivityEventType.PROFILE_UPDATED, "Profile picture removed");
    }

    @Test
    void rejectsOversizedInvalidAndDisabledAccountUploads() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> useCase.upload(ownerUuid,
                new byte[ProfilePictureImageValidator.MAX_BYTES + 1], "image/png"));
        assertThrows(IllegalArgumentException.class, () -> useCase.upload(ownerUuid, new byte[]{1}, "image/gif"));
        owner.setEnabled(false);
        assertThrows(ProfilePictureAccessException.class, () -> useCase.upload(ownerUuid, png(), "image/png"));
        verify(users, never()).save(any());
    }

    @Test
    void onlyLooksUpTheUuidProvidedByTheAuthenticatedCurrentUserFlow() throws Exception {
        UUID otherUuid = UUID.randomUUID();
        when(users.findByUuid(otherUuid)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.upload(otherUuid, png(), "image/png"));
        verify(users).findByUuid(otherUuid);
        verify(users, never()).save(any());
    }

    @Test
    void removesNewStorageObjectIfTheDatabaseTransactionRollsBack() throws Exception {
        TransactionSynchronizationManager.initSynchronization();
        try {
            String newKey = useCase.upload(ownerUuid, png(), "image/png").pictureUrl();
            verify(storage, never()).delete(anyString());
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            }
            verify(storage).delete(newKey);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private byte[] png() throws Exception {
        return image("png");
    }

    private byte[] image(String format) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    private static User user(UUID uuid) {
        User user = new User();
        user.setUuid(uuid);
        user.setUsername(new Username("owner"));
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        return user;
    }
}
