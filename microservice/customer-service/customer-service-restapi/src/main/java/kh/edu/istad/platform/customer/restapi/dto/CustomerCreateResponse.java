package kh.edu.istad.platform.customer.restapi.dto;

import java.util.UUID;

public record CustomerCreateResponse (
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String status,
        UUID segmentId
){
}
