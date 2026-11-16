package kh.edu.istad.platform.iam.domain.usecase.permission;

import java.util.Locale;
import java.util.regex.Pattern;

/** Shared normalization and consistency rules for permission definitions. */
final class PermissionDefinitionValidator {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*");
    private PermissionDefinitionValidator() {}

    static Definition validate(String name, String resource, String action, String description) {
        String normalizedName = normalize(name, "Permission name");
        String normalizedResource = normalize(resource, "Permission resource");
        String normalizedAction = normalize(action, "Permission action");
        if (normalizedName.length() > 100 || normalizedResource.length() > 100 || normalizedAction.length() > 100)
            throw new IllegalArgumentException("Permission name, resource, and action must be at most 100 characters");
        if (description != null && description.length() > 500)
            throw new IllegalArgumentException("Permission description must be at most 500 characters");
        if (!TOKEN.matcher(normalizedName).matches()) throw new IllegalArgumentException("Permission name has an invalid format");
        if (!TOKEN.matcher(normalizedResource).matches()) throw new IllegalArgumentException("Permission resource has an invalid format");
        if (!TOKEN.matcher(normalizedAction).matches()) throw new IllegalArgumentException("Permission action has an invalid format");
        if (!(normalizedResource + "." + normalizedAction).equals(normalizedName))
            throw new IllegalArgumentException("Permission name must match resource.action");
        return new Definition(normalizedName, normalizedResource, normalizedAction);
    }

    private static String normalize(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required");
        return value.trim().toLowerCase(Locale.ROOT);
    }

    record Definition(String name, String resource, String action) {}
}
