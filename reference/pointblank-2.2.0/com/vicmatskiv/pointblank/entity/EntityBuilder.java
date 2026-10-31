/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.Level
 */
package com.vicmatskiv.pointblank.entity;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.client.EntityRendererBuilder;
import com.vicmatskiv.pointblank.item.EffectBuilderInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public interface EntityBuilder<T extends EntityBuilder<T, E>, E extends Entity> {
    public static EntityBuilder<?, ?> fromZipEntry(ZipFile zipFile, ZipEntry entry, boolean isClientSide) {
        EntityBuilder<?, ?> entityBuilder;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        try {
            entityBuilder = EntityBuilder.fromReader(reader, isClientSide);
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        reader.close();
        return entityBuilder;
    }

    public static EntityBuilder<?, ?> fromPath(Path path, boolean isClientSide) {
        EntityBuilder<?, ?> entityBuilder;
        block8: {
            BufferedReader br = Files.newBufferedReader(path);
            try {
                entityBuilder = EntityBuilder.fromReader(br, isClientSide);
                if (br == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (br != null) {
                        try {
                            br.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            br.close();
        }
        return entityBuilder;
    }

    public static EntityBuilder<?, ?> fromReader(Reader reader, boolean isClientSide) {
        throw new UnsupportedOperationException();
    }

    public T withJsonObject(JsonObject var1);

    public String getName();

    public EntityTypeExt getEntityTypeExt();

    public EntityType.Builder<E> getEntityTypeBuilder();

    public boolean hasRenderer();

    public EntityRenderer<?> createEntityRenderer(EntityRendererProvider.Context var1);

    public EntityBuilder<?, ?> withName(String var1);

    public EntityBuilder<?, ?> withItem(Supplier<Item> var1);

    public EntityBuilder<?, ?> withInitialVelocity(double var1);

    public EntityBuilder<?, ?> withMaxLifetime(long var1);

    public EntityBuilder<?, ?> withRenderer(Supplier<EntityRendererBuilder<?, Entity, EntityRenderer<Entity>>> var1);

    public EntityBuilder<?, ?> withGravity(double var1);

    public EntityBuilder<?, ?> withEffect(EffectBuilderInfo var1);

    public EntityBuilder<?, ?> withRicochet(boolean var1);

    public E build(Level var1);

    public static enum EntityTypeExt {
        PROJECTILE;

    }
}

