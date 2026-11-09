package kh.edu.istad.platform.iam.domain.valueobject;

public record Address(
        String street,
        String city,
        String province,
        String country,
        String postalCode
) {
}
