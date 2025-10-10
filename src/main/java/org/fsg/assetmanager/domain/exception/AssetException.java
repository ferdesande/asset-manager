package org.fsg.assetmanager.domain.exception;

public sealed class AssetException extends RuntimeException
        permits AssetAlreadyExistsException {
    public AssetException() {
    }

    public AssetException(String message) {
        super(message);
    }

    public AssetException(String message, Throwable cause) {
        super(message, cause);
    }
}
