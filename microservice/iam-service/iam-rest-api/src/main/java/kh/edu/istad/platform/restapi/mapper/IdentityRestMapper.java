package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.restapi.dto.response.IdentityResponse;
import org.springframework.stereotype.Component;

@Component
public class IdentityRestMapper {

    public IdentityResponse toResponse(Identity identity) {
        return new IdentityResponse(
                identity.getUuid(),
                null,
                identity.getProvider().name(),
                identity.getProviderSubject().value(),
                identity.getCreatedAt(),
                identity.getUpdatedAt()
        );
    }
}