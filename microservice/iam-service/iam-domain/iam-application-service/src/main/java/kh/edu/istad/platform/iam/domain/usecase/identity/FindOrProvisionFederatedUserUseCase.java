package kh.edu.istad.platform.iam.domain.usecase.identity;

import kh.edu.istad.platform.iam.domain.dto.command.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.usecase.user.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.IdentityProvider;
import kh.edu.istad.platform.iam.domain.valueobject.ProviderSubject;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class FindOrProvisionFederatedUserUseCase {

    private final IdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final GetUserAuthoritiesUseCase getUserAuthoritiesUseCase;

    public FindOrProvisionFederatedUserUseCase(
            IdentityRepository identityRepository,
            UserRepository userRepository,
            GetUserAuthoritiesUseCase getUserAuthoritiesUseCase
    ) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.getUserAuthoritiesUseCase = getUserAuthoritiesUseCase;
    }

    public AuthUserResult execute(FederatedIdentityCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Federated identity command is required"
            );
        }

        if (command.provider() == null || command.provider().isBlank()) {
            throw new IllegalArgumentException(
                    "Identity provider is required"
            );
        }

        if (command.providerSubject() == null
                || command.providerSubject().isBlank()) {
            throw new IllegalArgumentException(
                    "Provider subject is required"
            );
        }

        if (command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        IdentityProvider provider;

        try {
            provider = IdentityProvider.valueOf(
                    command.provider().trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported identity provider",
                    exception
            );
        }

        String providerSubject = command.providerSubject().trim();
        String email = command.email().trim().toLowerCase();

        Identity existingIdentity = identityRepository
                .findByProviderAndProviderSubject(
                        IdentityProvider.valueOf(String.valueOf(provider)),
                        providerSubject
                )
                .orElse(null);

        User user;

        if (existingIdentity != null) {

            if (existingIdentity.isDeleted()) {
                throw new IllegalStateException(
                        "Federated identity has been deleted"
                );
            }

            user = userRepository.findById(existingIdentity.getUserId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Associated user not found"
                    ));

            if (user.isDeleted() || !user.isEnabled()) {
                throw new IllegalStateException(
                        "User account is unavailable"
                );
            }

            existingIdentity.setLastLoginAt(Instant.now());
            existingIdentity.setUpdatedAt(Instant.now());

            identityRepository.save(existingIdentity);

        } else {

            // 2. Check whether a local user already has this email.
            user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {

                // 3. Create a new user.
                String username = command.username();

                if (username == null || username.isBlank()) {
                    username = email.substring(
                            0,
                            email.indexOf('@')
                    );
                }

                String baseUsername = username.trim();
                username = baseUsername;

                int suffix = 1;

                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + suffix;
                    suffix++;
                }

                Instant now = Instant.now();

                user = new User();

                user.setUuid(UUID.randomUUID());
                user.setUsername(new Username(username));
                user.setEmail(new Email(email));
                user.setEmailVerified(command.emailVerified());
                user.setEnabled(true);
                user.setAccountNonExpired(true);
                user.setAccountNonLocked(true);
                user.setCredentialsNonExpired(true);
                user.setFailedLoginAttempts(0);
                user.setCreatedAt(now);

                user = userRepository.save(user);

            } else if (user.isDeleted() || !user.isEnabled()) {

                throw new IllegalStateException(
                        "An unavailable user account already uses this email"
                );
            }

            // 4. Link the federated identity to the user.
            Instant now = Instant.now();

            Identity identity = new Identity();

            identity.setUuid(UUID.randomUUID());
            identity.setUserId(user.getId());
            identity.setProvider(provider);
            identity.setProviderSubject(
                    new ProviderSubject(providerSubject)
            );
            identity.setEmail(new Email(email));
            identity.setEmailVerified(command.emailVerified());
            identity.setUsername(user.getUsername());
            identity.setDisplayName(command.displayName());
            identity.setPictureUrl(command.pictureUrl());
            identity.setAttributes(command.attributes());
            identity.setLinkedAt(now);
            identity.setLastLoginAt(now);
            identity.setDeleted(false);
            identity.setCreatedAt(now);

            identityRepository.save(identity);
        }

        // 5. Load the user's authorities.
        var authorities = getUserAuthoritiesUseCase.execute(user.getUuid());
        return new AuthUserResult(
                user.getUuid(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.isEnabled(),
                !user.isAccountNonLocked(),
                authorities.roles().stream()
                        .map(role -> "ROLE_" + role)
                        .toList(),
                authorities.permissions()
        );
    }
}
