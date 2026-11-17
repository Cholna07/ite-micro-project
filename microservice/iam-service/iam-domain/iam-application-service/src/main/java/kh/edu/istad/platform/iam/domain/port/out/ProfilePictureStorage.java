package kh.edu.istad.platform.iam.domain.port.out;

import java.util.Optional;

/** Storage boundary for user supplied profile pictures. Keys are opaque and never filesystem paths. */
public interface ProfilePictureStorage {
    String store(byte[] content, String extension);

    Optional<StoredPicture> read(String key);

    void delete(String key);

    boolean isManagedKey(String key);

    record StoredPicture(byte[] content, String mediaType) {
    }
}
