package dev.sorokin.eventmanager.repository.specification;

import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.service.model.EventSearchFilter;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventSpecifications {

    @SuppressWarnings({"java:S5612", "java:S3776"})
    public static Specification<EventEntity> fromFilter(EventSearchFilter filter) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("owner", JoinType.LEFT);
                root.fetch("location", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                predicates.add(cb.equal(root.get("name"), filter.name()));
            }
            if (filter.placesMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxPlaces"), filter.placesMin()));
            }
            if (filter.placesMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxPlaces"), filter.placesMax()));
            }
            if (filter.dateStartAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), filter.dateStartAfter()));
            }
            if (filter.dateStartBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), filter.dateStartBefore()));
            }
            if (filter.costMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("cost"), filter.costMin()));
            }
            if (filter.costMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("cost"), filter.costMax()));
            }
            if (filter.durationMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("durationMinutes"), filter.durationMin()));
            }
            if (filter.durationMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("durationMinutes"), filter.durationMax()));
            }
            if (filter.locationId() != null) {
                predicates.add(cb.equal(root.get("location").get("id"), filter.locationId()));
            }
            if (filter.eventStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.eventStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
