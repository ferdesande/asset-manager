package org.fsg.assetmanager.domain.model;

import java.time.Instant;

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
        return copy(AssetStatus.PUBLISHED, publishedUrl);
    }

    public Asset markAsFailed() {
        return copy(AssetStatus.FAILED, null);
    }

    private Asset copy(AssetStatus updatedStatus, String updatedPublishedUrl) {
        return new Asset(id, filename, contentType, fileSize, uploadDate, updatedStatus, updatedPublishedUrl);
    }
}
