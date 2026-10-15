package kh.edu.istad.platform.order.domain.valueobject;

public record StreetAddress(
        String street,
        String postalCode,
        String city
) {
    public StreetAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street is required");
        }

        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code is required");
        }

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
    }
}