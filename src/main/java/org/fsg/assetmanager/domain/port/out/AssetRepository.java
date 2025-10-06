package org.fsg.assetmanager.domain.port.out;

import org.fsg.assetmanager.domain.model.Asset;

import java.util.List;

public interface AssetRepository {
    Asset save(Asset asset);

    List<Asset> find(AssetSearchCriteria criteria);
}
