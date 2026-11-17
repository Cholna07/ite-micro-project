package kh.edu.istad.platform.iam.domain.port.out;

import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.dto.result.LoginAttemptUpdate;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    // User retrieval
    Optional<User> findById(Long id);
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    PageResult<User> findAll(UserFilterQuery query);

    // User creation and update
    User save(User user);

    LoginAttemptUpdate recordFailedLoginAttempt(UUID userUuid, int lockoutThreshold);

    boolean recordSuccessfulLogin(UUID userUuid, java.time.Instant lastLoginAt);

    // User deletion
    void delete(User user);

    // Duplicate checking
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
