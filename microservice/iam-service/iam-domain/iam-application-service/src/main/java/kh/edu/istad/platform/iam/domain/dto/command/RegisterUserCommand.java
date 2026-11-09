package kh.edu.istad.platform.iam.domain.dto.command;

public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String givenName,
        String familyName
) {
}
