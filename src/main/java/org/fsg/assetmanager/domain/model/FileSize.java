package org.fsg.assetmanager.domain.model;

public record FileSize(int value) {
    public FileSize {
        if (value < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }
    }
}
