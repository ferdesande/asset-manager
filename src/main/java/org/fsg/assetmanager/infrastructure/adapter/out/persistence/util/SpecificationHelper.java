package org.fsg.assetmanager.infrastructure.adapter.out.persistence.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationHelper {
    public static <T> Specification<T> ilike(String fieldName, String pattern) {
        return ilike(fieldName, pattern, 0);
    }

    public static <T> Specification<T> ilike(String fieldName, String pattern, Integer minLength) {
        return ((root, query, criteriaBuilder) -> {
            if (pattern == null || pattern.isBlank() || pattern.length() < minLength) {
                return criteriaBuilder.conjunction();
            }

            // SqmCriteriaNodeBuilder implements native Postgres ilike. if it's not present,
            // a less performant way is used.
            if (criteriaBuilder instanceof SqmCriteriaNodeBuilder cb) {
                return cb.ilike(root.get(fieldName), "%" + pattern.toLowerCase() + "%");
            } else {
                return criteriaBuilder.like(root.get(fieldName), "%" + pattern.toLowerCase() + "%");
            }
        });
    }
}
