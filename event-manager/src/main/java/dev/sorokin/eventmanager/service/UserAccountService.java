package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.repository.mapper.UserDbMapper;
import dev.sorokin.eventmanager.service.exception.UserNotFoundException;
import dev.sorokin.eventmanager.service.model.UserAccount;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserDbMapper mapper;

    @Transactional
    public UserAccount createUser(UserAccount userAccount) {
        if (userAccountRepository.existsByLogin(userAccount.login())) {
            throw new IllegalArgumentException("Login '%s' is already taken".formatted(userAccount.login()));
        }
        UserEntity entity = mapper.toEntity(userAccount);
        UserEntity createdUser = userAccountRepository.save(entity);
        return mapper.toDomain(createdUser);
    }

    @Transactional
    public UserAccount getUser(Long id) {
        UserEntity entity = userAccountRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapper.toDomain(entity);
    }

    @Transactional
    public UserAccount getUserByLogin(String login) {
        UserEntity entity = userAccountRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));
        return mapper.toDomain(entity);
    }

    @Transactional
    public List<UserAccount> getAllUsers() {
        List<UserEntity> entities = userAccountRepository.findAll();
        return mapper.toDomain(entities);
    }
}
