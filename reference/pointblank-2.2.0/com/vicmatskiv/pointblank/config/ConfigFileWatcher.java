/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.config;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigFileWatcher
implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ConfigFileWatcher.class);
    private static final long DEBOUNCE_DELAY_MS = 150L;
    private final Path configPath;
    private final Runnable onFileChanged;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicLong lastModified = new AtomicLong(0L);
    private Thread watcherThread;
    private WatchService watchService;

    public ConfigFileWatcher(Path configPath, Runnable onFileChanged) {
        this.configPath = configPath;
        this.onFileChanged = onFileChanged;
    }

    public void start() {
        if (this.watcherThread != null && this.watcherThread.isAlive()) {
            LOGGER.warn("File watcher is already running for: {}", (Object)this.configPath);
            return;
        }
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            Path parentDir = this.configPath.getParent();
            if (parentDir != null && Files.exists(parentDir, new LinkOption[0])) {
                parentDir.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                this.watcherThread = new Thread((Runnable)this, "PointBlank-ConfigWatcher");
                this.watcherThread.setDaemon(true);
                this.running.set(true);
                this.watcherThread.start();
                LOGGER.debug("Started config file watcher for: {}", (Object)this.configPath);
            } else {
                LOGGER.warn("Cannot watch config file, parent directory does not exist: {}", (Object)parentDir);
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to start config file watcher for: {}", (Object)this.configPath, (Object)e);
        }
    }

    public void stop() {
        this.running.set(false);
        if (this.watchService != null) {
            try {
                this.watchService.close();
            }
            catch (IOException e) {
                LOGGER.debug("Error closing watch service", (Throwable)e);
            }
        }
        if (this.watcherThread != null) {
            this.watcherThread.interrupt();
            try {
                this.watcherThread.join(1000L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.debug("Stopped config file watcher for: {}", (Object)this.configPath);
    }

    @Override
    public void run() {
        String fileName = this.configPath.getFileName().toString();
        while (this.running.get()) {
            try {
                WatchKey key = this.watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    long last;
                    long now;
                    WatchEvent<?> pathEvent;
                    Path changedFile;
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW || !(changedFile = (Path)(pathEvent = event).context()).toString().equals(fileName) || (now = System.currentTimeMillis()) - (last = this.lastModified.get()) <= 150L) continue;
                    this.lastModified.set(now);
                    Thread.sleep(150L);
                    LOGGER.debug("Config file changed: {}", (Object)this.configPath);
                    try {
                        this.onFileChanged.run();
                    }
                    catch (Exception e) {
                        LOGGER.error("Error handling config file change", (Throwable)e);
                    }
                }
                boolean valid = key.reset();
                if (valid) continue;
                LOGGER.warn("Watch key is no longer valid for: {}", (Object)this.configPath);
                break;
            }
            catch (InterruptedException e) {
                if (this.running.get()) {
                    LOGGER.debug("Config file watcher interrupted");
                }
                Thread.currentThread().interrupt();
                break;
            }
            catch (ClosedWatchServiceException e) {
                break;
            }
            catch (Exception e) {
                LOGGER.error("Unexpected error in config file watcher", (Throwable)e);
            }
        }
    }

    public void pauseNotifications() {
        this.lastModified.set(System.currentTimeMillis() + 1000L);
    }
}

