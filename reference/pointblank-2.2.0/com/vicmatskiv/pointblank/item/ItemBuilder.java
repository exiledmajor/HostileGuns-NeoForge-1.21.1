/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.world.item.Item
 */
package com.vicmatskiv.pointblank.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.entity.EntityBuilderProvider;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.AttachmentItem;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.MiscItem;
import com.vicmatskiv.pointblank.item.ThrowableItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.world.item.Item;

public abstract class ItemBuilder<T extends ItemBuilder<T>>
implements Nameable {
    public static ItemBuilder<?> fromZipEntry(ZipFile zipFile, ZipEntry entry, boolean isClientSide) {
        ItemBuilder<?> itemBuilder;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
        try {
            itemBuilder = ItemBuilder.fromReader(reader, isClientSide);
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
        return itemBuilder;
    }

    public static ItemBuilder<?> fromPath(Path path, boolean isClientSide) {
        ItemBuilder<?> itemBuilder;
        block8: {
            BufferedReader br = Files.newBufferedReader(path);
            try {
                itemBuilder = ItemBuilder.fromReader(br, isClientSide);
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
        return itemBuilder;
    }

    public static ItemBuilder<?> fromReader(Reader reader, boolean isClientSide) {
        try {
            JsonObject obj = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            String itemTypeStr = obj.has("type") ? obj.getAsJsonPrimitive("type").getAsString() : ItemType.GUN.toString();
            ItemType itemType = ItemType.valueOf(itemTypeStr.toUpperCase(Locale.ROOT));
            if (itemType == ItemType.GUN) {
                return new GunItem.Builder().withJsonObject(obj, isClientSide);
            }
            if (itemType == ItemType.AMMO) {
                return new AmmoItem.Builder().withJsonObject(obj, isClientSide);
            }
            if (itemType == ItemType.THROWABLE) {
                return new ThrowableItem.Builder().withJsonObject(obj, isClientSide);
            }
            if (itemType == ItemType.MISC) {
                return new MiscItem.MiscItemBuilder().withJsonObject(obj, isClientSide);
            }
            if (itemType == ItemType.ATTACHMENT) {
                return new AttachmentItem.Builder().withJsonObject(obj, isClientSide);
            }
            throw new IllegalArgumentException("Invalid item type: " + String.valueOf((Object)itemType));
        }
        catch (Exception e) {
            throw new RuntimeException("Error parsing JSON: " + e.getMessage(), e);
        }
    }

    public abstract T withJsonObject(JsonObject var1, boolean var2);

    public EntityBuilderProvider getEntityBuilderProvider() {
        return null;
    }

    public abstract Item build();

    public static enum ItemType {
        GUN,
        AMMO,
        MISC,
        ATTACHMENT,
        THROWABLE;

    }
}

