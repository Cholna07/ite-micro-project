package kh.edu.istad.platform.iam.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalProfilePictureStorageTest {
    @TempDir Path directory;

    @Test
    void storesReadsAndDeletesOpaqueKeysWithoutTrustingSuppliedPath() throws Exception {
        LocalProfilePictureStorage storage = new LocalProfilePictureStorage(directory.toString());
        byte[] content = {1, 2, 3};
        String key = storage.store(content, "png");

        assertTrue(key.startsWith("profile-picture:"));
        assertTrue(storage.isManagedKey(key));
        assertArrayEquals(content, storage.read(key).orElseThrow().content());
        assertEquals("image/png", storage.read(key).orElseThrow().mediaType());
        assertFalse(storage.isManagedKey("profile-picture:../../secret.png"));
        assertTrue(storage.read("profile-picture:../../secret.png").isEmpty());

        storage.delete(key);
        assertTrue(storage.read(key).isEmpty());
        assertEquals(0, Files.list(directory).count());
    }
}
