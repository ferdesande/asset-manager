package org.fsg.assetmanager.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import org.fsg.assetmanager.domain.exception.AssetAlreadyExistsException;
import org.fsg.assetmanager.domain.model.Asset;
import org.fsg.assetmanager.domain.model.SortDirection;
import org.fsg.assetmanager.domain.port.out.AssetRepository;
import org.fsg.assetmanager.domain.port.out.AssetSearchCriteria;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.AssetJpaRepository;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.entity.AssetEntity;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.mapper.AssetEntityMapper;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.util.SpecificationHelper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@AllArgsConstructor
public class AssetRepositoryAdapter implements AssetRepository {

    private AssetJpaRepository repository;
    private AssetEntityMapper mapper;

    @Override
    public Asset save(Asset asset) {
        if (asset == null) {
            return null;
        }

        try {
            AssetEntity entity = mapper.toEntity(asset);
            AssetEntity saved = repository.save(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new AssetAlreadyExistsException(asset.id(), e);
        }
    }

    @Override
    public List<Asset> find(AssetSearchCriteria criteria) {
        Specification<AssetEntity> specification = buildSpecification(criteria);
        Sort sort = buildSort(criteria);
        return repository.findAll(specification, sort)
                .stream()
                .map(entity -> mapper.toDomain(entity))
                .toList();
    }

    private Specification<AssetEntity> buildSpecification(AssetSearchCriteria criteria) {
        return Specification.allOf(
                AssetSpecification.uploadDateRange(criteria.uploadDateStart(), criteria.uploadDateEnd()),
                AssetSpecification.filenameMatches(criteria.filename()),
                AssetSpecification.contentTypeEquals(criteria.contentType())
        );
    }

    private Sort buildSort(AssetSearchCriteria criteria) {
        Sort.Direction direction = criteria.sortDirection() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // By default, sort is done by upload date. There is no spec about the sorting field.
        return Sort.by(direction, "uploadDate");
    }

    static final class AssetSpecification {
        private static final int MIN_LIKE_FILTER_LENGTH = 3;

        static Specification<AssetEntity> uploadDateRange(Instant start, Instant end) {
            return (root, criteriaQuery, criteriaBuilder) -> {
                if (start != null && end != null) {
                    return criteriaBuilder.between(root.get("uploadDate"), start, end);
                } else if (start != null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("uploadDate"), start);
                } else if (end != null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("uploadDate"), end);
                } else {
                    return criteriaBuilder.conjunction(); // No filter (Where 1=1)
                }
            };
        }

        static Specification<AssetEntity> filenameMatches(String pattern) {
            return SpecificationHelper.ilike("filename", pattern, MIN_LIKE_FILTER_LENGTH);
        }

        static Specification<AssetEntity> contentTypeEquals(String contentType) {
            return (root, query, criteriaBuilder) -> {
                if (contentType == null || contentType.isBlank()) {
                    return criteriaBuilder.conjunction();
                }
                return criteriaBuilder.equal(root.get("contentType"), contentType.toLowerCase());
            };
        }
    }
}
