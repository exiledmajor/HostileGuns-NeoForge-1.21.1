/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  net.minecraft.ChatFormatting
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.client.gui.screens.ConfirmScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.CommonComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.client.gui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.gui.ExtensionPackSelectionList;
import com.vicmatskiv.pointblank.client.gui.NotificationToast;
import com.vicmatskiv.pointblank.registry.ExtensionRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExtensionPackScreen
extends Screen {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private final Screen parent;
    private ExtensionPackSelectionList packList;
    private Component notificationDialog = null;
    private int notificationDialogTimer = 0;
    private Button openFolderButton;
    private Button removeButton;
    private Button browseButton;

    public ExtensionPackScreen(Screen parent) {
        super((Component)Component.translatable((String)"screen.pointblank.packs.title"));
        this.parent = parent;
    }

    protected void init() {
        super.init();
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonGap = 8;
        int totalWidth = buttonWidth * 3 + buttonGap * 2;
        int minPadding = 10;
        int buttonsY = this.height - 27;
        int startX = Math.max(minPadding, (this.width - totalWidth) / 2);
        if (startX + totalWidth > this.width - minPadding) {
            startX = Math.max(minPadding, this.width - minPadding - totalWidth);
        }
        this.packList = new ExtensionPackSelectionList(this, this.minecraft, this.width, this.height, 20, this.height - 46);
        this.addRenderableWidget((GuiEventListener)this.packList);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)CommonComponents.GUI_DONE, button -> this.onClose()).bounds(startX + (buttonWidth + buttonGap) * 2, buttonsY, buttonWidth, buttonHeight).build());
        this.openFolderButton = Button.builder((Component)Component.translatable((String)"screen.pointblank.packs.openFolder"), button -> this.openPackFolder()).bounds(startX, buttonsY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget((GuiEventListener)this.openFolderButton);
        this.removeButton = Button.builder((Component)Component.translatable((String)"screen.pointblank.packs.remove"), button -> this.confirmRemoveSelectedPack()).bounds(startX + buttonWidth + buttonGap, buttonsY, buttonWidth, buttonHeight).build();
        this.removeButton.active = false;
        this.addRenderableWidget((GuiEventListener)this.removeButton);
        int browseButtonSize = 20;
        int titleWidth = this.font.width((FormattedText)this.title);
        int browseButtonX = this.width / 2 + titleWidth / 2 + 4;
        this.browseButton = Button.builder((Component)Component.literal((String)"\ud83d\udd0d"), button -> this.openBrowseUrl()).bounds(browseButtonX, 3, browseButtonSize, browseButtonSize).tooltip(Tooltip.create((Component)Component.translatable((String)"screen.pointblank.packs.browse.tooltip"))).build();
        this.addRenderableWidget((GuiEventListener)this.browseButton);
    }

    public void tick() {
        super.tick();
        if (this.notificationDialogTimer > 0) {
            --this.notificationDialogTimer;
        }
        if (this.packList != null) {
            ExtensionPackSelectionList.PackEntry selected = this.packList.getSelectedPack();
            this.removeButton.active = selected != null;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.packList.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 6, -1);
        if (this.notificationDialog != null && this.notificationDialogTimer > 0) {
            guiGraphics.drawCenteredString(this.font, this.notificationDialog, this.width / 2, this.height - 40, -1);
        } else {
            MutableComponent hint = Component.translatable((String)"screen.pointblank.packs.dragHint").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC});
            guiGraphics.drawCenteredString(this.font, (Component)hint, this.width / 2, this.height - 40, 0xAAAAAA);
        }
    }

    public void onFilesDrop(List<Path> paths) {
        List<Path> validPacks = paths.stream().filter(this::isValidExtensionPack).toList();
        for (Path pack : validPacks) {
            String fileName = pack.getFileName().toString();
            try {
                this.copyPackToDirectory(pack);
            }
            catch (FileAlreadyExistsException e) {
                this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.copyErrorAlreadyExists", (Object[])new Object[]{fileName}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.RED});
                this.notificationDialogTimer = 100;
                return;
            }
            catch (DuplicatePackNameException e) {
                this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.copyErrorDuplicateName", (Object[])new Object[]{e.getPackName()}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.RED});
                this.notificationDialogTimer = 100;
                return;
            }
            catch (IOException e) {
                LOGGER.warn("Error copying dragged extension pack", (Throwable)e);
                this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.copyError", (Object[])new Object[]{fileName}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.RED});
                this.notificationDialogTimer = 100;
                return;
            }
        }
        if (this.packList != null) {
            this.packList.refresh();
        }
        if (validPacks.isEmpty()) {
            if (paths.size() == 1) {
                String fileName = paths.get(0).getFileName().toString();
                this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.failedAddSingle", (Object[])new Object[]{fileName}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.RED});
            } else {
                this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.failedAdd").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.RED});
            }
        } else if (validPacks.size() == 1) {
            String packName = validPacks.get(0).getFileName().toString();
            this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.addedPack", (Object[])new Object[]{packName}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.YELLOW});
            this.showRestartRequiredNotification();
        } else {
            this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.addedPacks", (Object[])new Object[]{validPacks.size()}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.YELLOW});
            this.showRestartRequiredNotification();
        }
        this.notificationDialogTimer = 100;
    }

    private boolean isValidExtensionPack(Path path) {
        if (Files.isDirectory(path, new LinkOption[0])) {
            return Files.exists(path.resolve("ext.json"), new LinkOption[0]);
        }
        if (path.toString().endsWith(".zip")) {
            boolean bl;
            ZipFile zipFile = new ZipFile(path.toFile());
            try {
                bl = zipFile.getEntry("ext.json") != null;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        zipFile.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    return false;
                }
            }
            zipFile.close();
            return bl;
        }
        return false;
    }

    private void copyPackToDirectory(Path sourcePath) throws IOException, DuplicatePackNameException {
        Set<String> existingNames;
        String fileName;
        Path targetDir = ExtensionRegistry.getExtensionsPath();
        Path targetPath = targetDir.resolve(fileName = sourcePath.getFileName().toString());
        if (Files.exists(targetPath, new LinkOption[0])) {
            throw new FileAlreadyExistsException(fileName);
        }
        String newPackName = this.getPackNameFromSource(sourcePath);
        if (newPackName != null && (existingNames = this.getExistingPackNames()).contains(newPackName.toLowerCase())) {
            throw new DuplicatePackNameException(newPackName);
        }
        if (Files.isDirectory(sourcePath, new LinkOption[0])) {
            this.copyDirectory(sourcePath, targetPath);
        } else {
            Files.copy(sourcePath, targetPath, new CopyOption[0]);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String getPackNameFromSource(Path sourcePath) {
        try {
            if (Files.isDirectory(sourcePath, new LinkOption[0])) {
                Path extJson = sourcePath.resolve("ext.json");
                if (!Files.exists(extJson, new LinkOption[0])) return null;
                try (BufferedReader reader = Files.newBufferedReader(extJson);){
                    JsonObject json = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
                    if (json == null) return null;
                    if (!json.has("name")) return null;
                    String string = json.get("name").getAsString();
                    return string;
                }
            }
            if (!sourcePath.toString().endsWith(".zip")) return null;
            try (ZipFile zipFile = new ZipFile(sourcePath.toFile());){
                ZipEntry extEntry = zipFile.getEntry("ext.json");
                if (extEntry == null) return null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(extEntry)));){
                    JsonObject json = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
                    if (json == null) return null;
                    if (!json.has("name")) return null;
                    String string = json.get("name").getAsString();
                    return string;
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("Could not read pack name from {}", (Object)sourcePath, (Object)e);
        }
        return null;
    }

    private Set<String> getExistingPackNames() {
        HashSet<String> names = new HashSet<String>();
        Path extensionsPath = ExtensionRegistry.getExtensionsPath();
        if (!Files.exists(extensionsPath, new LinkOption[0]) || !Files.isDirectory(extensionsPath, new LinkOption[0])) {
            return names;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath);){
            for (Path entry : stream) {
                String name = this.getPackNameFromSource(entry);
                if (name == null) continue;
                names.add(name.toLowerCase());
            }
        }
        catch (IOException e) {
            LOGGER.debug("Error scanning existing pack names", (Throwable)e);
        }
        return names;
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target, new FileAttribute[0]);
        try (Stream<Path> stream = Files.walk(source, new FileVisitOption[0]);){
            stream.forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize((Path)sourcePath));
                    if (Files.isDirectory(sourcePath, new LinkOption[0])) {
                        Files.createDirectories(targetPath, new FileAttribute[0]);
                    } else {
                        Files.copy(sourcePath, targetPath, new CopyOption[0]);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void confirmRemoveSelectedPack() {
        ExtensionPackSelectionList.PackEntry selected = this.packList.getSelectedPack();
        if (selected == null) {
            return;
        }
        String packName = selected.getPackName();
        this.minecraft.setScreen((Screen)new ConfirmScreen(confirmed -> {
            if (confirmed) {
                this.removeSelectedPack(selected);
            }
            this.minecraft.setScreen((Screen)this);
        }, (Component)Component.translatable((String)"screen.pointblank.packs.confirmRemove.title"), (Component)Component.translatable((String)"screen.pointblank.packs.confirmRemove.message", (Object[])new Object[]{packName})));
    }

    private void removeSelectedPack(ExtensionPackSelectionList.PackEntry entry) {
        try {
            Path packPath = entry.getPackPath();
            if (packPath != null) {
                if (Files.isDirectory(packPath, new LinkOption[0])) {
                    this.deleteDirectory(packPath);
                } else {
                    Files.deleteIfExists(packPath);
                }
                this.packList.refresh();
                this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.removed", (Object[])new Object[]{entry.getPackName()}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.YELLOW});
                this.notificationDialogTimer = 100;
                this.showRestartRequiredNotification();
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to remove extension pack", (Throwable)e);
            this.notificationDialog = Component.translatable((String)"screen.pointblank.packs.removeError", (Object[])new Object[]{entry.getPackName()}).withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.RED});
            this.notificationDialogTimer = 100;
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> stream = Files.walk(directory, new FileVisitOption[0]);){
            stream.sorted((a, b) -> b.compareTo((Path)a)).forEach(path -> {
                try {
                    Files.delete(path);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void showRestartRequiredNotification() {
        MutableComponent toastMessage = Component.translatable((String)"screen.pointblank.packs.restartRequired.title").append(": ").append((Component)Component.translatable((String)"screen.pointblank.packs.restartRequired.message"));
        Minecraft.getInstance().getToasts().addToast((Toast)new NotificationToast((Component)toastMessage, 5000L));
    }

    private void openPackFolder() {
        CompletableFuture.runAsync(() -> {
            Path packPath = ExtensionRegistry.getExtensionsPath();
            try {
                if (!Files.exists(packPath, new LinkOption[0])) {
                    Files.createDirectories(packPath, new FileAttribute[0]);
                }
                Util.getPlatform().openUri(packPath.toUri());
            }
            catch (IOException e) {
                LOGGER.error("Failed to open pack folder", (Throwable)e);
            }
        });
    }

    private void openBrowseUrl() {
        String url = Config.extensionPackBrowseUrl;
        if (url != null && !url.isEmpty()) {
            try {
                Util.getPlatform().openUri(url);
            }
            catch (Exception e) {
                LOGGER.error("Failed to open browse URL", (Throwable)e);
            }
        }
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void displayNotification(Component component) {
        this.notificationDialog = component;
        this.notificationDialogTimer = 100;
    }

    private static class DuplicatePackNameException
    extends Exception {
        private final String packName;

        public DuplicatePackNameException(String packName) {
            super("Pack with name '" + packName + "' already exists");
            this.packName = packName;
        }

        public String getPackName() {
            return this.packName;
        }
    }
}

