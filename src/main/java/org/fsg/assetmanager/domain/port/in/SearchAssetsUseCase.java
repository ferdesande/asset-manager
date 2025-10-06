package org.fsg.assetmanager.domain.port.in;

import org.fsg.assetmanager.domain.model.Asset;

import java.util.List;

public interface SearchAssetsUseCase {
    List<Asset> search(AssetSearchQuery query);
}
