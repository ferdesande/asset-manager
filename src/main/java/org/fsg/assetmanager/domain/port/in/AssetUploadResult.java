package org.fsg.assetmanager.domain.port.in;

import org.fsg.assetmanager.domain.model.AssetId;

public record AssetUploadResult(
        AssetId assetId
) {
}
