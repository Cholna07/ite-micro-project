package kh.edu.istad.platform.iam.domain.usecase.identity;

import kh.edu.istad.platform.iam.domain.dto.command.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.domain.dto.result.IdentityResult;
import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.IdentityProvider;
import kh.edu.istad.platform.iam.domain.valueobject.ProviderSubject;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
@Component
public class LinkIdentityUseCase {

    private final UserRepository userRepository;
    private final IdentityRepository identityRepository;

    public LinkIdentityUseCase(
            UserRepository userRepository,
            IdentityRepository identityRepository
    ) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
    }

    public IdentityResult execute(
            UUID userUuid,
            FederatedIdentityCommand command
    ) {

        if (userUuid == null) {
            throw new IllegalArgumentException("User UUID is required");
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "Federated identity command is required"
            );
        }

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        if (user.isDeleted() || !user.isEnabled()) {
            throw new IllegalStateException(
                    "User account is unavailable"
            );
        }

        if (command.provider() == null || command.provider().isBlank()
                || command.providerSubject() == null
                || command.providerSubject().isBlank()
                || command.email() == null
                || command.email().isBlank()) {
            throw new IllegalArgumentException(
                    "Provider, provider subject, and email are required"
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

        if (identityRepository.existsByProviderAndProviderSubject(
                provider.name(),
                providerSubject
        )) {
            throw new IllegalStateException(
                    "This federated identity is already linked"
            );
        }

        String email = command.email().trim().toLowerCase();

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

        Identity savedIdentity = identityRepository.save(identity);

        return new IdentityResult(
                savedIdentity.getUuid(),
                savedIdentity.getProvider().name(),
                savedIdentity.getProviderSubject().value(),
                savedIdentity.getEmail().value(),
                savedIdentity.isEmailVerified(),
                savedIdentity.getDisplayName(),
                savedIdentity.getPictureUrl(),
                savedIdentity.getLinkedAt(),
                savedIdentity.getLastLoginAt()
        );
    }
}