package dev.sorokin.eventmanager.repository;

import dev.sorokin.eventmanager.common.exception.UserNotFoundException;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByLogin(String login);

    /** Loads a user by id or raises a 404. */
    default UserEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    /** Loads a user by login or raises a 404; the standard lookup for resolving the authenticated caller. */
    default UserEntity getByLoginOrThrow(String login) {
        return findByLogin(login).orElseThrow(() -> new UserNotFoundException(login));
    }

    boolean existsByLogin(String login);

    boolean existsByRole(UserRole role);
}
