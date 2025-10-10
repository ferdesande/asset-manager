package org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa;

import org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetJpaRepository extends
        JpaRepository<AssetEntity, Long>,
        JpaSpecificationExecutor<AssetEntity> {
}
