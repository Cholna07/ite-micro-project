package kh.edu.istad.platform.iam.domain.port.out;
import kh.edu.istad.platform.iam.domain.entity.Identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository {

    Optional<Identity> findByUuid(UUID uuid);

    Optional<Identity> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );

    List<Identity> findByUserId(Long userId);

    Identity save(Identity identity);

    boolean existsByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );
}