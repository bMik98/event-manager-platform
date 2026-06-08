package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.repository.mapper.UserDbMapper;
import dev.sorokin.eventmanager.service.exception.UserAlreadyExistsException;
import dev.sorokin.eventmanager.service.exception.UserNotFoundException;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserDbMapper mapper;

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
        UserEntity entity = userAccountRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public UserAccount getUserByLogin(String login) {
        UserEntity entity = userAccountRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> getAllUsers() {
        List<UserEntity> entities = userAccountRepository.findAll();
        return mapper.toDomain(entities);
    }
}
