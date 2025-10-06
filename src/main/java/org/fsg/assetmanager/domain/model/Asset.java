package org.fsg.assetmanager.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Asset {
    private final AssetId id;
    private final Filename filename;
    private final ContentType contentType;
    private final FileSize fileSize;
    private final Instant uploadDate;

    @Setter(AccessLevel.PRIVATE)
    private AssetStatus status;

    @Setter(AccessLevel.PRIVATE)
    private String publishedUrl;

    public static Asset create(Filename filename, ContentType contentType, FileSize fileSize) {
        return Asset.builder()
                .id(new AssetId(UUID.randomUUID().toString()))
                .filename(filename)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadDate(Instant.now())
                .status(AssetStatus.PENDING)
                .publishedUrl(null)
                .build();
    }
}
