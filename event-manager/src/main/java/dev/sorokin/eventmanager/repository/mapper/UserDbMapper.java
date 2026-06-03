package dev.sorokin.eventmanager.repository.mapper;

import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.service.model.UserAccount;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDbMapper {

    UserEntity toEntity(UserAccount userAccount);

    UserAccount toDomain(UserEntity entity);

    List<UserAccount> toDomain(List<UserEntity> entityList);
}
