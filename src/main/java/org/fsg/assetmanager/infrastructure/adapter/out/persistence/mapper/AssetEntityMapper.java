package org.fsg.assetmanager.infrastructure.adapter.out.persistence.mapper;

import org.fsg.assetmanager.domain.model.*;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.entity.AssetEntity;
import org.springframework.stereotype.Component;

@Component
public class AssetEntityMapper {
    public AssetEntity toEntity(Asset asset) {
        if (asset == null) {
            return null;
        }

        String lowerContentType = (asset.contentType() != null && asset.contentType().value() != null)
                ? asset.contentType().value().toLowerCase()
                : null;

        return AssetEntity.builder()
                .externalId(asset.id().value())
                .filename(asset.filename().value())
                .contentType(lowerContentType)
                .size((long) asset.fileSize().value())
                .uploadDate(asset.uploadDate())
                .status(asset.status())
                .url(asset.publishedUrl())
                .build();
    }

    public Asset toDomain(AssetEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Asset(
                new AssetId((entity.getExternalId())),
                new Filename(entity.getFilename()),
                new ContentType(entity.getContentType()),
                new FileSize(Math.toIntExact(entity.getSize())),
                entity.getUploadDate(),
                entity.getStatus(),
                entity.getUrl()
        );
    }
}
