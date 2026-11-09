package kh.edu.istad.platform.iam.domain.dto.command;

public record LoginUserCommand(
        String usernameOrEmail,
        String password
) {
}