package dev.sorokin.eventmanager.repository.mapper;

import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.service.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationDbMapper {

    @Mapping(target = "version", ignore = true)
    LocationEntity toEntity(Location location);

    Location toDomain(LocationEntity entity);

    List<Location> toDomain(List<LocationEntity> entityList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDomain(Location sourceDomain, @MappingTarget LocationEntity targetEntity);
}
