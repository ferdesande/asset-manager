package org.fsg.assetmanager.domain.port.out;

import org.fsg.assetmanager.domain.model.SortDirection;

import java.time.Instant;

public record AssetSearchCriteria(
        Instant uploadDateStart,
        Instant uploadDateEnd,
        String filename,
        String contentType,
        SortDirection sortDirection
) {
}
