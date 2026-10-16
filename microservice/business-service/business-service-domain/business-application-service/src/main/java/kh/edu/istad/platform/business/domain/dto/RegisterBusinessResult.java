package kh.edu.istad.platform.business.domain.dto;

public record RegisterBusinessResult(
        String businessId,
        String legalName,
        String displayName,
        String email,
        String phoneNumber,
        boolean active
) {
}