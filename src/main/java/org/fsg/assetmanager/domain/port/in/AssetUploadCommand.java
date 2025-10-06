package org.fsg.assetmanager.domain.port.in;

public record AssetUploadCommand(
        String filename,
        String contentType,
        int size,
        byte[] bytes
) {
}
