package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserSummary;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ListUsersUseCase {

    private final UserRepository userRepository;

    public ListUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResult<UserSummary> execute(UserFilterQuery query) {

        if (query == null) {
            throw new IllegalArgumentException(
                    "User filter query cannot be null"
            );
        }

        if (query.page() < 0) throw new IllegalArgumentException("Page index cannot be negative");
        if (query.size() < 1 || query.size() > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");

        PageResult<kh.edu.istad.platform.iam.domain.entity.User> users = userRepository.findAll(query);

        List<UserSummary> content = users.content().stream()
                .map(user -> new UserSummary(
                        user.getUuid(),
                        user.getUsername().value(),
                        user.getEmail() == null ? null : user.getEmail().value(),
                        user.getGivenName(),
                        user.getFamilyName(),
                        user.isEnabled(),
                        user.isEmailVerified()
                ))
                .toList();

        return new PageResult<>(content, users.page(), users.size(), users.totalElements(), users.totalPages(),
                users.first(), users.last());
    }
}
