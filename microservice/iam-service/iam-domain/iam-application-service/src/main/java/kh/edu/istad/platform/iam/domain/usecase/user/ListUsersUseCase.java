package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.UserSummary;
import kh.edu.istad.platform.iam.domain.entity.User;
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

        List<User> users = userRepository.findAll(query);

        List<UserSummary> content = users.stream()
                .map(user -> new UserSummary(
                        user.getUuid(),
                        user.getUsername().value(),
                        user.getEmail().value(),
                        user.getGivenName(),
                        user.getFamilyName(),
                        user.isEnabled(),
                        user.isEmailVerified()
                ))
                .toList();

        return new PageResult<>(
                content,
                query.page(),
                query.size(),
                content.size(),
                1,
                query.page() == 0,
                true
        );
    }
}
