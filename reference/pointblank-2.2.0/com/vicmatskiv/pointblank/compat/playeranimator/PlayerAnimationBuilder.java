/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface PlayerAnimationBuilder {
    public String getName();

    public Supplier<InputStream> getReaderFactory();

    public static PlayerAnimationBuilder fromPath(final Path path) {
        Path fileNamePath = path.getFileName();
        String fileName = fileNamePath.toString();
        if (fileName.endsWith(".animation.json")) {
            fileName = fileName.substring(0, fileName.length() - 15);
        }
        final String name = fileName;
        return new PlayerAnimationBuilder(){

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Supplier<InputStream> getReaderFactory() {
                return () -> {
                    try {
                        return Files.newInputStream(path, new OpenOption[0]);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        };
    }

    public static PlayerAnimationBuilder fromZipEntry(final ZipFile zipFile, final ZipEntry entry) {
        final String name = entry.getName().endsWith(".animation.json") ? entry.getName().substring(0, entry.getName().length() - 15) : entry.getName();
        return new PlayerAnimationBuilder(){

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Supplier<InputStream> getReaderFactory() {
                return () -> {
                    try {
                        return zipFile.getInputStream(entry);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        };
    }
}

