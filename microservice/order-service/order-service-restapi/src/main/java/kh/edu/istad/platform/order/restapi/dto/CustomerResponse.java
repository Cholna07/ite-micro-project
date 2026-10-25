package kh.edu.istad.platform.order.restapi.dto;

public record CustomerResponse(
        EmailValue email,
        String familyName,
        String givenName,
        IdValue id,
        PhoneNumberValue phoneNumber,
        SegmentIdValue segmentId,
        String status,
        String username
) {
    public record EmailValue(String value) {}
    public record IdValue(String value) {}
    public record PhoneNumberValue(String value) {}
    public record SegmentIdValue(String value) {}
}
