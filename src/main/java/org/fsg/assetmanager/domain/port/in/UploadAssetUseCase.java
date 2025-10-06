package org.fsg.assetmanager.domain.port.in;

public interface UploadAssetUseCase {
    AssetUploadResult upload(AssetUploadCommand command);
}
