/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSelectionList$Entry
 *  net.minecraft.client.gui.components.ObjectSelectionList
 *  net.minecraft.client.gui.components.ObjectSelectionList$Entry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.client.gui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vicmatskiv.pointblank.client.gui.ExtensionPackScreen;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemBuilder;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import com.vicmatskiv.pointblank.registry.ExtensionRegistry;
import com.vicmatskiv.pointblank.registry.GunRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExtensionPackSelectionList
extends ObjectSelectionList<Entry> {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final Component PACK_LIST_LABEL = Component.translatable((String)"screen.pointblank.packs.list.label").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY});
    private static final Component NO_PACKS_LABEL = Component.translatable((String)"screen.pointblank.packs.noPacks").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY});
    private final ExtensionPackScreen screen;

    public ExtensionPackSelectionList(ExtensionPackScreen screen, Minecraft minecraft, int width, int height, int top, int bottom) {
        super(minecraft, width, bottom - top, top, 30);
        this.screen = screen;
        this.refresh();
    }

    public void refresh() {
        this.clearEntries();
        List<PackInfo> packs = this.scanExtensionPacks();
        if (packs.isEmpty()) {
            this.addEntry((AbstractSelectionList.Entry)new LabelEntry(NO_PACKS_LABEL));
        } else {
            for (PackInfo pack : packs) {
                this.addEntry((AbstractSelectionList.Entry)new PackEntry(pack));
            }
            this.addEntry((AbstractSelectionList.Entry)new LabelEntry(PACK_LIST_LABEL));
        }
    }

    private List<PackInfo> scanExtensionPacks() {
        ArrayList<PackInfo> packs = new ArrayList<PackInfo>();
        Path extensionsPath = ExtensionRegistry.getExtensionsPath();
        if (!Files.exists(extensionsPath, new LinkOption[0]) || !Files.isDirectory(extensionsPath, new LinkOption[0])) {
            return packs;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath);){
            for (Path entry : stream) {
                PackInfo info = null;
                if (Files.isDirectory(entry, new LinkOption[0])) {
                    info = this.loadPackInfoFromDirectory(entry);
                } else if (entry.toString().endsWith(".zip")) {
                    info = this.loadPackInfoFromZip(entry);
                }
                if (info == null) continue;
                packs.add(info);
            }
        }
        catch (IOException e) {
            LOGGER.error("Error scanning extension packs", (Throwable)e);
        }
        return packs;
    }

    private PackInfo loadPackInfoFromDirectory(Path dirPath) {
        PackInfo packInfo;
        block9: {
            Path extJson = dirPath.resolve("ext.json");
            if (!Files.exists(extJson, new LinkOption[0])) {
                return null;
            }
            BufferedReader reader = Files.newBufferedReader(extJson);
            try {
                JsonObject json = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
                String name = json.has("name") ? json.get("name").getAsString() : dirPath.getFileName().toString();
                String description = json.has("description") ? json.get("description").getAsString() : null;
                String author = json.has("author") ? json.get("author").getAsString() : null;
                String version = json.has("version") ? json.get("version").getAsString() : null;
                String creativeTabIconItem = json.has("creativeTabIconItem") ? json.get("creativeTabIconItem").getAsString() : null;
                String displayName = this.loadDisplayNameFromPackMcmeta(dirPath);
                String fallbackGunItemName = this.findFirstGunItemName(dirPath);
                packInfo = new PackInfo(name, displayName, description, author, version, creativeTabIconItem, fallbackGunItemName, dirPath, false);
                if (reader == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    LOGGER.error("Error reading ext.json from {}", (Object)dirPath, (Object)e);
                    return null;
                }
            }
            reader.close();
        }
        return packInfo;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String loadDisplayNameFromPackMcmeta(Path dirPath) {
        Path packMcmeta = dirPath.resolve("pack.mcmeta");
        if (!Files.exists(packMcmeta, new LinkOption[0])) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(packMcmeta);){
            JsonObject json = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
            if (!json.has("pack")) return null;
            JsonObject pack = json.getAsJsonObject("pack");
            if (!pack.has("description")) return null;
            String string = pack.get("description").getAsString();
            return string;
        }
        catch (IOException e) {
            LOGGER.debug("Could not read pack.mcmeta from {}", (Object)dirPath);
            return null;
        }
        catch (JsonSyntaxException e) {
            LOGGER.warn("Malformed pack.mcmeta in {}: {}", (Object)dirPath, (Object)e.getMessage());
        }
        return null;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private PackInfo loadPackInfoFromZip(Path zipPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile());){
            PackInfo packInfo;
            ZipEntry extEntry = zipFile.getEntry("ext.json");
            if (extEntry == null) {
                PackInfo packInfo2 = null;
                return packInfo2;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(extEntry)));){
                JsonObject json = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
                String name = json.has("name") ? json.get("name").getAsString() : zipPath.getFileName().toString();
                String description = json.has("description") ? json.get("description").getAsString() : null;
                String author = json.has("author") ? json.get("author").getAsString() : null;
                String version = json.has("version") ? json.get("version").getAsString() : null;
                String creativeTabIconItem = json.has("creativeTabIconItem") ? json.get("creativeTabIconItem").getAsString() : null;
                String displayName = this.loadDisplayNameFromZipPackMcmeta(zipFile);
                String fallbackGunItemName = this.findFirstGunItemName(zipFile);
                packInfo = new PackInfo(name, displayName, description, author, version, creativeTabIconItem, fallbackGunItemName, zipPath, true);
            }
            return packInfo;
        }
        catch (IOException e) {
            LOGGER.error("Error reading ext.json from zip {}", (Object)zipPath, (Object)e);
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String findFirstGunItemName(Path dirPath) {
        Path itemsPath = dirPath.resolve("assets").resolve("pointblank").resolve("items");
        if (!Files.exists(itemsPath, new LinkOption[0])) return null;
        if (!Files.isDirectory(itemsPath, new LinkOption[0])) {
            return null;
        }
        try (DirectoryStream<Path> itemFiles = Files.newDirectoryStream(itemsPath, "*.json");){
            Iterator<Path> iterator = itemFiles.iterator();
            while (iterator.hasNext()) {
                Path itemFile = iterator.next();
                try {
                    ItemBuilder<?> itemBuilder = ItemBuilder.fromPath(itemFile, true);
                    if (!(itemBuilder instanceof GunItem.Builder)) continue;
                    String string = itemBuilder.getName();
                    return string;
                }
                catch (Exception e) {
                    LOGGER.debug("Failed to read item definition {}", (Object)itemFile, (Object)e);
                }
            }
            return null;
        }
        catch (IOException e) {
            LOGGER.debug("Could not scan items directory for {}", (Object)dirPath, (Object)e);
        }
        return null;
    }

    private String findFirstGunItemName(ZipFile zipFile) {
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("assets/pointblank/items/") || !name.endsWith(".json")) continue;
                try {
                    ItemBuilder<?> itemBuilder = ItemBuilder.fromZipEntry(zipFile, entry, true);
                    if (!(itemBuilder instanceof GunItem.Builder)) continue;
                    return itemBuilder.getName();
                }
                catch (Exception e) {
                    LOGGER.debug("Failed to read item definition from zip entry {}", (Object)name, (Object)e);
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("Could not scan zip for item definitions", (Throwable)e);
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String loadDisplayNameFromZipPackMcmeta(ZipFile zipFile) {
        ZipEntry packMcmetaEntry = zipFile.getEntry("pack.mcmeta");
        if (packMcmetaEntry == null) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(packMcmetaEntry)));){
            JsonObject json = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
            if (!json.has("pack")) return null;
            JsonObject pack = json.getAsJsonObject("pack");
            if (!pack.has("description")) return null;
            String string = pack.get("description").getAsString();
            return string;
        }
        catch (IOException e) {
            LOGGER.debug("Could not read pack.mcmeta from zip");
        }
        return null;
    }

    public int getRowWidth() {
        return Math.min(400, this.width - 50);
    }

    protected int getScrollbarPosition() {
        return this.width - 6;
    }

    public PackEntry getSelectedPack() {
        Entry selected = (Entry)this.getSelected();
        if (selected instanceof PackEntry) {
            return (PackEntry)selected;
        }
        return null;
    }

    public static class LabelEntry
    extends Entry {
        private final Component label;

        public LabelEntry(Component label) {
            this.label = label;
        }

        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawCenteredString(font, this.label, left + width / 2, top + (height - 9) / 2, -5592406);
        }

        public Component getNarration() {
            return this.label;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
    }

    public static class PackInfo {
        private final String name;
        private final String displayName;
        private final String description;
        private final String author;
        private final String version;
        private final String creativeTabIconItem;
        private final String fallbackGunItemName;
        private final Path path;
        private final boolean isZip;

        public PackInfo(String name, String displayName, String description, String author, String version, String creativeTabIconItem, String fallbackGunItemName, Path path, boolean isZip) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.author = author;
            this.version = version;
            this.creativeTabIconItem = creativeTabIconItem;
            this.fallbackGunItemName = fallbackGunItemName;
            this.path = path;
            this.isZip = isZip;
        }

        public String getName() {
            return this.name;
        }

        public String getDisplayName() {
            if (this.displayName != null && !this.displayName.isEmpty()) {
                return this.displayName;
            }
            if (this.version != null && !this.version.isEmpty()) {
                return this.name + " v" + this.version;
            }
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        public String getAuthor() {
            return this.author;
        }

        public String getVersion() {
            return this.version;
        }

        public String getCreativeTabIconItem() {
            return this.creativeTabIconItem;
        }

        public String getFallbackGunItemName() {
            return this.fallbackGunItemName;
        }

        public Path getPath() {
            return this.path;
        }

        public boolean isZip() {
            return this.isZip;
        }

        public ItemStack getIconItemStack() {
            Supplier<? extends Item> supplier;
            Item item = null;
            if (this.creativeTabIconItem != null && !this.creativeTabIconItem.isEmpty() && (supplier = ItemRegistry.ITEMS.getItemsByName().get(this.creativeTabIconItem)) != null) {
                item = supplier.get();
            }
            if (item == null) {
                if (this.fallbackGunItemName != null && !this.fallbackGunItemName.isEmpty() && (supplier = ItemRegistry.ITEMS.getItemsByName().get(this.fallbackGunItemName)) != null) {
                    item = supplier.get();
                }
                if (item == null) {
                    item = GunRegistry.M4A1.get();
                }
                if (item == null) {
                    item = AmmoRegistry.AMMOCREATIVE.get();
                }
            }
            return new ItemStack((ItemLike)(item != null ? item : Items.AIR));
        }
    }

    public class PackEntry
    extends Entry {
        private final PackInfo packInfo;

        public PackEntry(PackInfo packInfo) {
            this.packInfo = packInfo;
        }

        public String getPackName() {
            return this.packInfo.getName();
        }

        public Path getPackPath() {
            return this.packInfo.getPath();
        }

        public PackInfo getPackInfo() {
            return this.packInfo;
        }

        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Object pathStr;
            boolean isSelected;
            Font font = Minecraft.getInstance().font;
            boolean bl = isSelected = ExtensionPackSelectionList.this.getSelected() == this;
            if (isSelected || isHovered) {
                int bgColor = isSelected ? -2130706433 : 0x40FFFFFF;
                guiGraphics.fill(left - 2, top - 2, left + width - 2, top + height + 2, bgColor);
            }
            int iconX = left + 4;
            int iconY = top + 5;
            int iconSize = 18;
            guiGraphics.fill(iconX - 1, iconY - 1, iconX + iconSize - 1, iconY + iconSize - 1, -7631989);
            guiGraphics.fill(iconX, iconY, iconX + iconSize - 2, iconY + iconSize - 2, -3750202);
            ItemStack iconStack = this.packInfo.getIconItemStack();
            guiGraphics.renderItem(iconStack, iconX, iconY);
            MutableComponent nameComponent = Component.literal((String)this.packInfo.getDisplayName());
            if (isSelected) {
                nameComponent = nameComponent.withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD});
            } else if (isHovered) {
                nameComponent = nameComponent.withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.BOLD});
            }
            int nameX = left + 28;
            int maxNameWidth = width - 38;
            int nameY = top + 4;
            Object displayName = this.packInfo.getDisplayName();
            if (font.width((String)displayName) > maxNameWidth) {
                displayName = font.plainSubstrByWidth((String)displayName, maxNameWidth - 10) + "...";
            }
            guiGraphics.drawString(font, (Component)nameComponent, nameX, nameY, -1);
            Object secondLine = "";
            if (this.packInfo.getDescription() != null && !this.packInfo.getDescription().isEmpty()) {
                secondLine = this.packInfo.getDescription();
            } else if (this.packInfo.getAuthor() != null && !this.packInfo.getAuthor().isEmpty()) {
                secondLine = "by " + this.packInfo.getAuthor();
            }
            if (!((String)secondLine).isEmpty()) {
                if (font.width((String)secondLine) > maxNameWidth) {
                    secondLine = font.plainSubstrByWidth((String)secondLine, maxNameWidth - 10) + "...";
                }
                guiGraphics.drawString(font, (String)secondLine, nameX, nameY + 10, -5592406, false);
            }
            if (font.width((String)(pathStr = this.packInfo.getPath().getFileName().toString())) > maxNameWidth) {
                pathStr = "..." + font.plainSubstrByWidth((String)pathStr, maxNameWidth - 20);
            }
            int pathY = ((String)secondLine).isEmpty() ? nameY + 12 : nameY + 20;
            int pathColor = isSelected ? -13948117 : -7829368;
            guiGraphics.drawString(font, (String)pathStr, nameX, pathY, pathColor, false);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                ExtensionPackSelectionList.this.setSelected((AbstractSelectionList.Entry)this);
                return true;
            }
            return false;
        }

        public Component getNarration() {
            return Component.literal((String)this.packInfo.getDisplayName());
        }
    }

    public static abstract class Entry
    extends ObjectSelectionList.Entry<Entry> {
    }
}

