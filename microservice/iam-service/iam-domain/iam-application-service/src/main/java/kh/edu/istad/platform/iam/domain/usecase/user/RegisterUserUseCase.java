package kh.edu.istad.platform.iam.domain.usecase.user;

import kh.edu.istad.platform.iam.domain.dto.command.RegisterUserCommand;
import kh.edu.istad.platform.iam.domain.dto.result.UserResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.iam.domain.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.domain.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.port.out.UserRoleRepository;
import kh.edu.istad.platform.iam.domain.port.out.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.domain.usecase.config.PasswordPolicy;
import kh.edu.istad.platform.iam.domain.usecase.config.RegistrationDisabledException;
import kh.edu.istad.platform.iam.domain.valueobject.Email;
import kh.edu.istad.platform.iam.domain.valueobject.Username;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider currentActorProvider;
    private final PasswordPolicy passwordPolicy;
    private final SystemConfigurationRepository configurationRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            CurrentActorProvider currentActorProvider,
            PasswordPolicy passwordPolicy,
            SystemConfigurationRepository configurationRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.currentActorProvider = currentActorProvider;
        this.passwordPolicy = passwordPolicy;
        this.configurationRepository = configurationRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public UserResult execute(RegisterUserCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Registration command cannot be null");
        }

        if (!configurationRepository.findCurrent().registrationEnabled())
            throw new RegistrationDisabledException();
        passwordPolicy.validate(command.password());

        // Check duplicate username
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateAccountException();
        }

        // Check duplicate email
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateAccountException();
        }

        // Create new user
        Instant now = Instant.now();

        User user = new User();

        user.setUuid(UUID.randomUUID());
        user.setUsername(new Username(command.username()));
        user.setEmail(new Email(command.email()));

        // Hash password
        user.setPasswordHash(passwordHasher.hash(command.password()));

        // Profile information
        user.setGivenName(command.givenName());
        user.setFamilyName(command.familyName());

        // Account status
        user.setEmailVerified(false);
        user.setPhoneNumberVerified(false);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setFailedLoginAttempts(0);

        // Audit information
        user.setCreatedAt(now);
        user.setCreatedBy(currentActorProvider.getCurrentActor());
        user.setVersion(0L);

        // Save user
        User savedUser = userRepository.save(user);

        Role defaultRole = roleRepository.findByName("USER")
                .filter(role -> role.isActive() && !role.isDeleted())
                .orElseThrow(() -> new IllegalStateException("Default USER role is unavailable"));
        UserRole assignment = new UserRole();
        assignment.setUserId(savedUser.getId());
        assignment.setRoleId(defaultRole.getId());
        assignment.setAssignedAt(now);
        assignment.setAssignedBy("SYSTEM");
        assignment.setReason("Default role assigned during local registration");
        userRoleRepository.save(assignment);

        // Return result
        return new UserResult(
                savedUser.getUuid(),
                savedUser.getUsername().value(),
                savedUser.getEmail().value(),
                savedUser.getGivenName(),
                savedUser.getFamilyName(),
                savedUser.getMiddleName(),
                savedUser.getNickname(),
                savedUser.getGender(),
                savedUser.getPhoneNumber() == null
                        ? null
                        : savedUser.getPhoneNumber().value(),
                savedUser.isEmailVerified(),
                savedUser.isPhoneNumberVerified(),
                savedUser.isEnabled(),
                !savedUser.isAccountNonLocked(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );
    }
}
