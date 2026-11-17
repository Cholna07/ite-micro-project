package kh.edu.istad.platform.iam.infrastructure.storage;

import kh.edu.istad.platform.iam.domain.port.out.ProfilePictureStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** Local-development adapter. Configure a durable object-storage adapter for multi-instance production. */
@Component
public class LocalProfilePictureStorage implements ProfilePictureStorage {
    private static final Pattern KEY = Pattern.compile("[0-9a-fA-F-]{36}\\.(jpg|png|webp)");
    private static final String KEY_PREFIX = "profile-picture:";
    private final Path root;

    public LocalProfilePictureStorage(@Value("${iam.storage.profile-pictures.directory:}") String configuredDirectory) {
        Path selected = configuredDirectory == null || configuredDirectory.isBlank()
                ? Path.of(System.getProperty("user.home"), ".northstar-iam", "profile-pictures")
                : Path.of(configuredDirectory);
        this.root = selected.toAbsolutePath().normalize();
    }

    @Override
    public String store(byte[] content, String extension) {
        String safeExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (!safeExtension.matches("jpg|png|webp")) {
            throw new IllegalArgumentException("Unsupported profile picture format");
        }
        String key = UUID.randomUUID() + "." + safeExtension;
        Path target = resolve(key);
        Path temporary = null;
        try {
            Files.createDirectories(root);
            temporary = Files.createTempFile(root, "upload-", ".tmp");
            Files.write(temporary, content);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, target);
            }
            return KEY_PREFIX + key;
        } catch (IOException exception) {
            throw new IllegalStateException("Profile picture storage is unavailable", exception);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }

    @Override
    public Optional<StoredPicture> read(String key) {
        String storageKey = unwrap(key);
        if (storageKey == null) return Optional.empty();
        try {
            Path path = resolve(storageKey);
            if (!Files.isRegularFile(path)) return Optional.empty();
            String extension = storageKey.substring(storageKey.lastIndexOf('.') + 1);
            String mediaType = switch (extension) {
                case "jpg" -> "image/jpeg";
                case "png" -> "image/png";
                case "webp" -> "image/webp";
                default -> throw new IllegalArgumentException("Unsupported profile picture format");
            };
            return Optional.of(new StoredPicture(Files.readAllBytes(path), mediaType));
        } catch (IOException exception) {
            throw new IllegalStateException("Profile picture could not be read", exception);
        }
    }

    @Override
    public void delete(String key) {
        String storageKey = unwrap(key);
        if (storageKey == null) return;
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Profile picture could not be removed from storage", exception);
        }
    }

    @Override
    public boolean isManagedKey(String key) {
        return unwrap(key) != null;
    }

    private Path resolve(String key) {
        if (!KEY.matcher(key).matches()) throw new IllegalArgumentException("Invalid profile picture key");
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) throw new IllegalArgumentException("Invalid profile picture key");
        return resolved;
    }

    private String unwrap(String key) {
        if (key == null || !key.startsWith(KEY_PREFIX)) return null;
        String storageKey = key.substring(KEY_PREFIX.length());
        return KEY.matcher(storageKey).matches() ? storageKey : null;
    }
}
