package org.fsg.assetmanager.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsg.assetmanager.domain.model.*;
import org.fsg.assetmanager.domain.port.in.*;
import org.fsg.assetmanager.domain.port.out.AssetPublisher;
import org.fsg.assetmanager.domain.port.out.AssetRepository;
import org.fsg.assetmanager.domain.port.out.AssetSearchCriteria;
import org.fsg.assetmanager.domain.port.out.PublishedUrl;
import org.fsg.assetmanager.domain.service.AssetValidator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public class AssetService implements UploadAssetUseCase, SearchAssetsUseCase {

    private final AssetValidator assetValidator;
    private final AssetRepository assetRepository;
    private final AssetPublisher assetPublisher;
    private final Clock clock;
    private final IdGenerator idGenerator;

    @Override
    public List<Asset> search(AssetSearchQuery query) {
        AssetSearchCriteria criteria = new AssetSearchCriteria(query.sortDirection());
        return assetRepository.find(criteria);
    }

    @Override
    public AssetUploadResult upload(AssetUploadCommand command) {
        Asset asset = createAsset(command, new Filename(command.filename()), new ContentType(command.contentType()));
        assetValidator.validate(asset);

        Asset savedAsset = assetRepository.save(asset);
        log.info("Asset info stored locally with ID '{}', filename: '{}'", asset.id(), asset.filename().value());

        uploadAsync(savedAsset, command.bytes());
        return new AssetUploadResult(savedAsset.id());
    }

    private Asset createAsset(AssetUploadCommand command, Filename filename, ContentType contentType) {
        return new Asset(
                new AssetId(idGenerator.generateId().toString()),
                filename,
                contentType,
                new FileSize(command.size()),
                clock.instant(),
                AssetStatus.PENDING,
                null);
    }

    @Async
    void uploadAsync(Asset asset, byte[] fileContent) {
        PublishedUrl published;
        try {
            published = assetPublisher.publish(asset, fileContent);
            log.info("Asset with ID '{}' was published successfully with url '{}'", asset.id(), published.url());
        } catch (Exception e) {
            assetRepository.save(asset.markAsFailed());
            log.error("Asset with ID '{}' failed to publish: {}", asset.id(), e.getMessage(), e);
            return;
        }

        try {
            assetRepository.save(asset.markAsPublished(published.url()));
            log.info("Asset with ID '{}' was marked as published", asset.id());
        } catch (Exception e) {
            log.error("CRITICAL: Asset with ID '{}' was published but failed to update metadata", asset.id());
        }
    }
}
