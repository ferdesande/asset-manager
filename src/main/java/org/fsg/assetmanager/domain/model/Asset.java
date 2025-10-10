package org.fsg.assetmanager.domain.model;

import lombok.With;

import java.time.Instant;

@With
public record Asset(
        AssetId id,
        Filename filename,
        ContentType contentType,
        FileSize fileSize,
        Instant uploadDate,
        AssetStatus status,
        String publishedUrl
) {

    public Asset markAsPublished(String publishedUrl) {
        return this
                .withStatus(AssetStatus.PUBLISHED)
                .withPublishedUrl(publishedUrl);
    }

    public Asset markAsFailed() {
        return this
                .withStatus(AssetStatus.FAILED)
                .withPublishedUrl(null);
    }
}
