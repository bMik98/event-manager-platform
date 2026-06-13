package dev.sorokin.eventmanager.security;

import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String login) throws UsernameNotFoundException {
        UserEntity userEntity = userAccountRepository.getByLoginOrThrow(login);
        return User.builder()
                .username(userEntity.getLogin())
                .password(userEntity.getPasswordHash())
                .authorities("ROLE_" + userEntity.getRole().name())
                .build();
    }
}
