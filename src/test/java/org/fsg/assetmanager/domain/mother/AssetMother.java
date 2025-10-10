package org.fsg.assetmanager.domain.mother;

import org.fsg.assetmanager.domain.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AssetMother {
    private static final String STORAGE_SERVER = "https//my-personal-storage/";

    public static final AssetId SAMPLE_ASSET_ID = new AssetId("00000000-0000-0000-0000-000000000001");
    public static final AssetId ANOTHER_ASSET_ID = new AssetId("00000000-0000-0000-0000-000000000002");

    public static final Filename SAMPLE_FILENAME = new Filename("sample filename");
    public static final Filename ANOTHER_FILENAME = new Filename("another filename");

    public static final ContentType SAMPLE_CONTENT_TYPE = new ContentType("sample content type");
    public static final ContentType ANOTHER_CONTENT_TYPE = new ContentType("another content type");

    public static final FileSize SAMPLE_FILE_SIZE = new FileSize(10);
    public static final FileSize ANOTHER_FILE_SIZE = new FileSize(17);

    public static final Instant SAMPLE_UPLOAD_DATE = LocalDateTime.of(2025, 6, 23, 14, 35).toInstant(ZoneOffset.UTC);
    public static final Instant ANOTHER_UPLOAD_DATE = LocalDateTime.of(2025, 7, 12, 7, 52).toInstant(ZoneOffset.UTC);

    public static final AssetStatus SAMPLE_STATUE = AssetStatus.PUBLISHED;
    public static final AssetStatus ANOTHER_STATUE = AssetStatus.PENDING;

    public static final byte[] SAMPLE_CONTENT = "sample content".getBytes();

    public static Asset SAMPLE_ASSET = new Asset(
            SAMPLE_ASSET_ID,
            SAMPLE_FILENAME,
            SAMPLE_CONTENT_TYPE,
            SAMPLE_FILE_SIZE,
            SAMPLE_UPLOAD_DATE,
            SAMPLE_STATUE,
            createPublishedUrl(SAMPLE_ASSET_ID));

    public static Asset ANOTHER_ASSET = new Asset(
            ANOTHER_ASSET_ID,
            ANOTHER_FILENAME,
            ANOTHER_CONTENT_TYPE,
            ANOTHER_FILE_SIZE,
            ANOTHER_UPLOAD_DATE,
            ANOTHER_STATUE,
            createPublishedUrl(ANOTHER_ASSET_ID));

    public static String createPublishedUrl(AssetId assetId) {
        return STORAGE_SERVER + assetId.value();
    }
}
