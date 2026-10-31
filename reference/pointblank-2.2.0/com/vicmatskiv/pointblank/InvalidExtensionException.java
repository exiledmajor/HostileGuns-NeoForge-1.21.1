/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank;

import java.nio.file.Path;

public class InvalidExtensionException
extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private Path path;

    public InvalidExtensionException(Path path, Throwable cause) {
        super(cause);
        this.path = path;
    }

    @Override
    public String getMessage() {
        return this.toString();
    }

    @Override
    public String toString() {
        return String.format("Invalid extension pack at path %s, error: %s", this.path, this.getCause());
    }
}

