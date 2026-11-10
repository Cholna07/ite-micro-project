package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.domain.dto.result.AuthUserResult;
import kh.edu.istad.platform.iam.domain.usecase.user.LoadUserForAuthenticationUseCase;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IamUserDetailsService implements UserDetailsService {

    private final LoadUserForAuthenticationUseCase loadUserForAuthenticationUseCase;

    public IamUserDetailsService(
            LoadUserForAuthenticationUseCase loadUserForAuthenticationUseCase
    ) {
        this.loadUserForAuthenticationUseCase = loadUserForAuthenticationUseCase;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AuthUserResult user;

        try {
            user = loadUserForAuthenticationUseCase.execute(username);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new UsernameNotFoundException(
                    "User not found or unavailable: " + username,
                    exception
            );
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        user.authorities().forEach(authority ->
                authorities.add(new SimpleGrantedAuthority(authority))
        );

        user.permissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission))
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.username())
                .password(user.passwordHash())
                .authorities(authorities)
                .disabled(!user.enabled())
                .accountLocked(user.accountLocked())
                .build();
    }
}
