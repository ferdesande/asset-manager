package org.fsg.assetmanager.domain.port.out;

import org.fsg.assetmanager.domain.model.Asset;

public interface AssetPublisher {
    PublishedUrl publish(Asset asset, byte[] content);
}
