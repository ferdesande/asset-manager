package org.fsg.assetmanager.domain.port.in;

import org.fsg.assetmanager.domain.model.SortDirection;

public record AssetSearchQuery(
        SortDirection sortDirection
) {
}
