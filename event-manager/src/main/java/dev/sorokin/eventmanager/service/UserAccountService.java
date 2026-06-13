package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.service.exception.InvalidCommandException;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.repository.mapper.UserDbMapper;
import dev.sorokin.eventmanager.service.exception.UserAlreadyExistsException;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserDbMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserAccount createUser(UserAccount userAccount) {
        if (userAccountRepository.existsByLogin(userAccount.login())) {
            throw new UserAlreadyExistsException(userAccount.login());
        }
        UserEntity entity = mapper.toEntity(userAccount);
        UserEntity createdUser = userAccountRepository.save(entity);
        return mapper.toDomain(createdUser);
    }

    @Transactional(readOnly = true)
    public boolean hasAdminAccount() {
        return userAccountRepository.existsByRole(UserRole.ADMIN);
    }

    @Transactional(readOnly = true)
    public UserAccount getUser(Long id) {
        UserEntity entity = userAccountRepository.getByIdOrThrow(id);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public UserAccount getUserByLogin(String login) {
        UserEntity entity = userAccountRepository.getByLoginOrThrow(login);
        return mapper.toDomain(entity);
    }

    /**
     * Verifies the supplied credentials and returns the authenticated account. The login lookup and the
     * password check are kept here in the service layer so the credential rules — and the
     * {@link InvalidCommandException} they raise — never leak into the web layer.
     */
    @Transactional(readOnly = true)
    public UserAccount authenticate(String login, String rawPassword) {
        UserAccount userAccount = getUserByLogin(login);
        if (!passwordEncoder.matches(rawPassword, userAccount.passwordHash())) {
            throw new InvalidCommandException("Invalid login or password");
        }
        return userAccount;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> getAllUsers() {
        List<UserEntity> entities = userAccountRepository.findAll();
        return mapper.toDomain(entities);
    }
}
