/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.config;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.config.BooleanOption;
import com.vicmatskiv.pointblank.config.ConfigChangeListener;
import com.vicmatskiv.pointblank.config.ConfigFileWatcher;
import com.vicmatskiv.pointblank.config.ConfigOption;
import com.vicmatskiv.pointblank.config.ConfigOptionBuilder;
import com.vicmatskiv.pointblank.config.ConfigUtil;
import com.vicmatskiv.pointblank.config.EnumOption;
import com.vicmatskiv.pointblank.config.ListOption;
import com.vicmatskiv.pointblank.config.NumberOption;
import com.vicmatskiv.pointblank.config.StringOption;
import com.vicmatskiv.pointblank.config.UnknownOption;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger(ConfigManager.class);
    private static final Map<String, ConfigManager> INSTANCES = new HashMap<String, ConfigManager>();
    private final String name;
    private final Path configPath;
    private final Map<String, ConfigOptionBuilder<?, ?>> optionBuilders;
    private final FutureOptionResolver futureOptionResolver;
    private final List<ConfigChangeListener> changeListeners = new CopyOnWriteArrayList<ConfigChangeListener>();
    private final Object lock = new Object();
    private ConfigFileWatcher fileWatcher;
    private volatile boolean isWriting = false;
    private Runnable onReloadCallback;

    public static ConfigManager getInstance(String configName) {
        return INSTANCES.get(configName);
    }

    public static Collection<ConfigManager> getAllInstances() {
        return Collections.unmodifiableCollection(INSTANCES.values());
    }

    private ConfigManager(String name, Map<String, ConfigOptionBuilder<?, ?>> optionBuilders, FutureOptionResolver futureOptionResolver) {
        this.name = name;
        this.optionBuilders = optionBuilders;
        this.futureOptionResolver = futureOptionResolver;
        Path gameDir = Platform.getInstance().getGameDir();
        Path configDirectory = gameDir.resolve("config");
        this.configPath = configDirectory.resolve(name + ".toml");
        this.handleConfigFile();
        this.startFileWatcher();
    }

    public void setOnReloadCallback(Runnable callback) {
        this.onReloadCallback = callback;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleConfigFile() {
        Object object = this.lock;
        synchronized (object) {
            LinkedHashMap configFileOptions = Files.exists(this.configPath, new LinkOption[0]) ? ConfigManager.parseConfig(this.configPath.toString(), this.optionBuilders) : new LinkedHashMap();
            Map<String, ConfigOption<?>> merged = this.merge(configFileOptions);
            if (!merged.equals(configFileOptions)) {
                try {
                    Files.createDirectories(this.configPath.getParent(), new FileAttribute[0]);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.writeConfigInternal(merged);
            }
            this.futureOptionResolver.options.clear();
            this.futureOptionResolver.options.putAll(merged);
        }
    }

    private void startFileWatcher() {
        this.fileWatcher = new ConfigFileWatcher(this.configPath, this::onFileChanged);
        this.fileWatcher.start();
    }

    private void onFileChanged() {
        if (this.isWriting) {
            return;
        }
        LOGGER.info("Config file changed externally, reloading: {}", (Object)this.configPath);
        this.reload();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reload() {
        Object object = this.lock;
        synchronized (object) {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                LOGGER.warn("Config file does not exist, skipping reload: {}", (Object)this.configPath);
                return;
            }
            HashMap oldValues = new HashMap();
            for (Map.Entry<String, ConfigOption<?>> entry : this.futureOptionResolver.options.entrySet()) {
                oldValues.put(entry.getKey(), entry.getValue().get());
            }
            Map<String, ConfigOption<?>> configFileOptions = ConfigManager.parseConfig(this.configPath.toString(), this.optionBuilders);
            Map<String, ConfigOption<?>> merged = this.merge(configFileOptions);
            this.futureOptionResolver.options.clear();
            this.futureOptionResolver.options.putAll(merged);
            for (Map.Entry<String, ConfigOption<?>> entry : merged.entrySet()) {
                String optionName = entry.getKey();
                Object newValue = entry.getValue().get();
                Object oldValue = oldValues.get(optionName);
                if (Objects.equals(oldValue, newValue)) continue;
                LOGGER.debug("Config value changed: {} = {} (was {})", (Object)optionName, newValue, oldValue);
                this.notifyListeners(optionName, oldValue, newValue);
            }
            if (this.onReloadCallback != null) {
                try {
                    this.onReloadCallback.run();
                }
                catch (Exception e) {
                    LOGGER.error("Error in reload callback", (Throwable)e);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean setValue(String optionName, Object value) {
        LOGGER.info("setValue called: {} = {}", (Object)optionName, value);
        Object object = this.lock;
        synchronized (object) {
            ConfigOption<?> existingOption = this.futureOptionResolver.options.get(optionName);
            if (existingOption == null) {
                LOGGER.warn("Attempted to set unknown config option: {} (available options: {})", (Object)optionName, this.futureOptionResolver.options.keySet());
                return false;
            }
            ConfigOptionBuilder<?, ?> builder = this.optionBuilders.get(optionName);
            if (builder == null) {
                LOGGER.warn("No builder found for config option: {}", (Object)optionName);
                return false;
            }
            Object oldValue = existingOption.get();
            Object normalizedValue = builder.normalize(value);
            LOGGER.debug("setValue: oldValue={}, normalizedValue={}", oldValue, normalizedValue);
            if (Objects.equals(oldValue, normalizedValue)) {
                LOGGER.debug("Config value unchanged: {} = {}", (Object)optionName, normalizedValue);
                return true;
            }
            ConfigOption<?> newOption = existingOption.createCopy(normalizedValue, existingOption.getIndex());
            this.futureOptionResolver.options.put(optionName, newOption);
            LOGGER.info("Config option updated in memory: {} = {} (was {})", (Object)optionName, normalizedValue, oldValue);
            this.saveToFile();
            this.notifyListeners(optionName, oldValue, normalizedValue);
            if (this.onReloadCallback != null) {
                try {
                    this.onReloadCallback.run();
                }
                catch (Exception e) {
                    LOGGER.error("Error in reload callback", (Throwable)e);
                }
            }
            LOGGER.info("Config value updated: {} = {} (was {})", (Object)optionName, normalizedValue, oldValue);
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object getValue(String optionName) {
        Object object = this.lock;
        synchronized (object) {
            ConfigOption<?> option = this.futureOptionResolver.options.get(optionName);
            return option != null ? option.get() : null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Set<String> getOptionNames() {
        Object object = this.lock;
        synchronized (object) {
            return Collections.unmodifiableSet(new LinkedHashSet<String>(this.futureOptionResolver.options.keySet()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveToFile() {
        Object object = this.lock;
        synchronized (object) {
            if (this.fileWatcher != null) {
                this.fileWatcher.pauseNotifications();
            }
            this.isWriting = true;
            try {
                this.writeConfigInternal(this.futureOptionResolver.options);
            }
            finally {
                this.isWriting = false;
            }
        }
    }

    public void addChangeListener(ConfigChangeListener listener) {
        this.changeListeners.add(listener);
    }

    public void removeChangeListener(ConfigChangeListener listener) {
        this.changeListeners.remove(listener);
    }

    private void notifyListeners(String optionName, Object oldValue, Object newValue) {
        for (ConfigChangeListener listener : this.changeListeners) {
            try {
                listener.onConfigChanged(optionName, oldValue, newValue);
            }
            catch (Exception e) {
                LOGGER.error("Error notifying config change listener", (Throwable)e);
            }
        }
    }

    public void shutdown() {
        if (this.fileWatcher != null) {
            this.fileWatcher.stop();
            this.fileWatcher = null;
        }
        INSTANCES.remove(this.name);
    }

    public static void shutdownAll() {
        for (ConfigManager manager : new ArrayList<ConfigManager>(INSTANCES.values())) {
            manager.shutdown();
        }
    }

    public String getName() {
        return this.name;
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    private Map<String, ConfigOption<?>> merge(Map<String, ConfigOption<?>> configFileOptions) {
        ArrayList declaredOptions = new ArrayList();
        for (ConfigOptionBuilder<?, ?> optionBuilder : this.optionBuilders.values()) {
            ConfigOption<?> refOption2 = optionBuilder.build();
            declaredOptions.add(refOption2);
        }
        List<ConfigOption> merged = ConfigUtil.mergeCollections(declaredOptions, configFileOptions.values(), ConfigOption::getName, Comparator.comparing(ConfigOption::getIndex), (refOption, fileOption) -> {
            if (refOption == null) {
                return null;
            }
            if (fileOption == null) {
                return refOption;
            }
            return refOption.copy((ConfigOption<?>)fileOption, fileOption.get());
        });
        return merged.stream().collect(Collectors.toMap(ConfigOption::getName, b -> b, (existing, replacement) -> replacement, LinkedHashMap::new));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeConfigInternal(Map<String, ConfigOption<?>> options) {
        Path tempPath = this.configPath.resolveSibling(String.valueOf(this.configPath.getFileName()) + ".tmp");
        LOGGER.info("Writing config to file: {}", (Object)this.configPath);
        try {
            Files.createDirectories(this.configPath.getParent(), new FileAttribute[0]);
            ConfigManager.writeConfigToPath(tempPath, options);
            try {
                Files.move(tempPath, this.configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (IOException | UnsupportedOperationException atomicException) {
                LOGGER.debug("Atomic move failed, using regular move: {}", (Object)atomicException.getMessage());
                Files.move(tempPath, this.configPath, StandardCopyOption.REPLACE_EXISTING);
            }
            LOGGER.info("Config file written successfully: {}", (Object)this.configPath);
        }
        catch (IOException e) {
            LOGGER.error("Failed to write config using temp file, attempting direct write", (Throwable)e);
            try {
                ConfigManager.writeConfigToPath(this.configPath, options);
                LOGGER.info("Config file written directly: {}", (Object)this.configPath);
            }
            catch (IOException ex) {
                LOGGER.error("Failed to write config file directly", (Throwable)ex);
                throw new RuntimeException("Failed to write config file: " + String.valueOf(this.configPath), ex);
            }
        }
        finally {
            try {
                Files.deleteIfExists(tempPath);
            }
            catch (IOException iOException) {}
        }
    }

    private static void writeConfigToPath(Path path, Map<String, ConfigOption<?>> options) throws IOException {
        LinkedHashMap<String, Map> tables = new LinkedHashMap<String, Map>();
        for (Map.Entry<String, ConfigOption<?>> entry : options.entrySet()) {
            String key;
            String table;
            String fullPath = entry.getKey();
            int lastDotIndex = fullPath.lastIndexOf(46);
            if (lastDotIndex == -1) {
                table = "";
                key = fullPath;
            } else {
                table = fullPath.substring(0, lastDotIndex);
                key = fullPath.substring(lastDotIndex + 1);
            }
            tables.computeIfAbsent(table, k -> new LinkedHashMap()).put(key, entry.getValue());
        }
        try (BufferedWriter br = new BufferedWriter(new FileWriter(path.toString()));){
            for (Map.Entry tableEntry : tables.entrySet()) {
                String table = (String)tableEntry.getKey();
                Map tableOptions = (Map)tableEntry.getValue();
                if (!table.isEmpty()) {
                    br.write("[" + table + "]");
                    br.write(System.lineSeparator());
                }
                for (Map.Entry optionEntry : tableOptions.entrySet()) {
                    ConfigOption option = (ConfigOption)optionEntry.getValue();
                    for (String s : option.getSerialized()) {
                        br.write(s);
                        br.write(System.lineSeparator());
                    }
                }
                br.write(System.lineSeparator());
            }
        }
    }

    private static Map<String, ConfigOption<?>> parseConfig(String filePath, Map<String, ConfigOptionBuilder<?, ?>> optionBuilders) {
        LinkedHashMap options = new LinkedHashMap();
        ArrayList<String> precedingLines = new ArrayList<String>();
        String currentPath = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));){
            String line;
            int optionIndex = 0;
            while ((line = br.readLine()) != null) {
                String fullPath;
                ConfigOptionBuilder<?, ?> optionBuilder;
                if ((line = line.trim()).startsWith("#")) {
                    precedingLines.add(line.substring(1).trim());
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]") && !line.contains("=")) {
                    currentPath = line.substring(1, line.length() - 1).trim();
                    continue;
                }
                if (!line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (!value.startsWith("[")) {
                    value = value.replaceAll("^\"|\"$", "");
                }
                if ((optionBuilder = optionBuilders.get(fullPath = currentPath.isEmpty() ? key : currentPath + "." + key)) != null) {
                    ConfigOption<?> option = optionBuilder.build(value, precedingLines, optionIndex);
                    options.put(fullPath, option);
                } else {
                    options.put(fullPath, new UnknownOption(fullPath, optionIndex, value, precedingLines));
                }
                precedingLines.clear();
                ++optionIndex;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return options;
    }

    private static class FutureOptionResolver {
        private final Map<String, ConfigOption<?>> options = new LinkedHashMap();

        private FutureOptionResolver() {
        }

        public Object getValue(String name) {
            ConfigOption<?> option = this.options.get(name);
            return option != null ? option.get() : null;
        }
    }

    public static class Builder {
        private String configName;
        private int optionCounter;
        private final List<ConfigOptionBuilder<?, ?>> optionBuilders = new ArrayList();
        private final FutureOptionResolver futureOptionResolver = new FutureOptionResolver();

        public Builder(String configName) {
            this.configName = configName;
        }

        public <N extends Number, B extends ConfigOptionBuilder<N, B>> ConfigOptionBuilder<N, B> createNumberOption(Class<N> cls, Function<String, N> converter) {
            int optionIndex = this.optionCounter++;
            Function<String, Number> futureOptionResolver = name -> (Number)this.futureOptionResolver.getValue((String)name);
            ConfigOptionBuilder optionBuilder = NumberOption.builder(cls, converter, futureOptionResolver, optionIndex);
            this.optionBuilders.add(optionBuilder);
            return optionBuilder;
        }

        public <B extends ConfigOptionBuilder<Integer, B>> ConfigOptionBuilder<Integer, B> createIntOption() {
            return this.createNumberOption(Integer.class, Integer::parseInt);
        }

        public <B extends ConfigOptionBuilder<Double, B>> ConfigOptionBuilder<Double, B> createDoubleOption() {
            return this.createNumberOption(Double.class, Double::parseDouble);
        }

        public <B extends ConfigOptionBuilder<Boolean, B>> ConfigOptionBuilder<Boolean, B> createBooleanOption() {
            int optionIndex = this.optionCounter++;
            Function<String, Boolean> futureOptionResolver = name -> (Boolean)this.futureOptionResolver.getValue((String)name);
            ConfigOptionBuilder optionBuilder = BooleanOption.builder(futureOptionResolver, optionIndex);
            this.optionBuilders.add(optionBuilder);
            return optionBuilder;
        }

        public <B extends ConfigOptionBuilder<String, B>> ConfigOptionBuilder<String, B> createStringOption() {
            int optionIndex = this.optionCounter++;
            Function<String, String> futureOptionResolver = name -> (String)this.futureOptionResolver.getValue((String)name);
            ConfigOptionBuilder optionBuilder = StringOption.builder(futureOptionResolver, optionIndex);
            this.optionBuilders.add(optionBuilder);
            return optionBuilder;
        }

        public <B extends ConfigOptionBuilder<List<String>, B>> ConfigOptionBuilder<List<String>, B> createListOption() {
            int optionIndex = this.optionCounter++;
            Function<String, List<String>> futureOptionResolver = name -> (List)this.futureOptionResolver.getValue((String)name);
            ConfigOptionBuilder optionBuilder = ListOption.builder(futureOptionResolver, optionIndex);
            this.optionBuilders.add(optionBuilder);
            return optionBuilder;
        }

        public <T extends Enum<T>, B extends ConfigOptionBuilder<T, B>> ConfigOptionBuilder<T, B> createEnumOption(Class<T> cls) {
            int optionIndex = this.optionCounter++;
            Function<String, Enum> futureOptionResolver = name -> (Enum)cls.cast(this.futureOptionResolver.getValue((String)name));
            ConfigOptionBuilder optionBuilder = EnumOption.builder(cls, futureOptionResolver, optionIndex);
            this.optionBuilders.add(optionBuilder);
            return optionBuilder;
        }

        public ConfigManager build() {
            ConfigManager manager = new ConfigManager(this.configName, this.optionBuilders.stream().collect(Collectors.toMap(ConfigOptionBuilder::getName, b -> b, (existing, replacement) -> replacement, LinkedHashMap::new)), this.futureOptionResolver);
            INSTANCES.put(this.configName, manager);
            return manager;
        }
    }
}

