package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.valueobject.Address;
import kh.edu.istad.platform.restapi.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserRestMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUuid(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getGivenName(),
                user.getFamilyName(),
                user.getGender(),
                user.getBirthdate(),
                user.getPhoneNumber() != null
                        ? user.getPhoneNumber().value()
                        : null,
                user.getZoneInfo(),
                user.getLocale(),
                toAddressResponse(user.getAddress()),
                user.isEnabled(),
                !user.isAccountNonLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserResponse.AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        return new UserResponse.AddressResponse(
                address.street(),
                address.city(),
                address.province(),
                address.postalCode(),
                address.country()
        );
    }
}
