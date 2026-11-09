package kh.edu.istad.platform.iam.domain.dto.command;

public record CreateUserCommand(
        String username,
        String email,
        String password,
        String givenName,
        String familyName,
        String middleName,
        String phoneNumber
) {
}
