package org.fsg.assetmanager.domain.port.out;

import org.fsg.assetmanager.domain.model.SortDirection;

public record AssetSearchCriteria(
        SortDirection sortDirection
) {
}
