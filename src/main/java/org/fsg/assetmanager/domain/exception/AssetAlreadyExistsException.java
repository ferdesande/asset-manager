package org.fsg.assetmanager.domain.exception;

import org.fsg.assetmanager.domain.model.AssetId;

public final class AssetAlreadyExistsException extends AssetException {
    private static final String MESSAGE_TEMPLATE = "Asset with ID '%s' already exists";

    public AssetAlreadyExistsException() {
    }

    public AssetAlreadyExistsException(AssetId assetId, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, assetId.value()), cause);
    }
}
