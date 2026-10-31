/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.FileUtil
 *  net.minecraft.SharedConstants
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.PackLocationInfo
 *  net.minecraft.server.packs.PackResources
 *  net.minecraft.server.packs.PackResources$ResourceOutput
 *  net.minecraft.server.packs.PackSelectionConfig
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.PathPackResources
 *  net.minecraft.server.packs.metadata.pack.PackMetadataSection
 *  net.minecraft.server.packs.repository.Pack
 *  net.minecraft.server.packs.repository.Pack$Position
 *  net.minecraft.server.packs.repository.Pack$ResourcesSupplier
 *  net.minecraft.server.packs.repository.PackSource
 *  net.minecraft.server.packs.repository.RepositorySource
 *  net.minecraft.server.packs.resources.IoSupplier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTab$Row
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.registry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.InvalidExtensionException;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationBuilder;
import com.vicmatskiv.pointblank.crafting.RecipeAdapter;
import com.vicmatskiv.pointblank.entity.EntityBuilder;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemBuilder;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import com.vicmatskiv.pointblank.registry.AmmoUiDefinition;
import com.vicmatskiv.pointblank.registry.AmmoUiRegistry;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.GunRegistry;
import com.vicmatskiv.pointblank.registry.HeatUiDefinition;
import com.vicmatskiv.pointblank.registry.HeatUiRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.registry.ThirdPersonAnimationRegistry;
import com.vicmatskiv.pointblank.util.JarFileSystem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExtensionRegistry {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private Pack extensionsPack;
    private List<Extension> extensions;
    private RepositorySource repositorySource = new RepositorySource(){

        public void loadPacks(Consumer<Pack> consumer) {
            if (ExtensionRegistry.this.extensionsPack != null) {
                consumer.accept(ExtensionRegistry.this.extensionsPack);
            }
        }
    };

    public static Path getExtensionsPath() {
        return Platform.getInstance().getGameDir().resolve("pointblank");
    }

    public List<Extension> getExtensions() {
        return Collections.unmodifiableList(this.extensions);
    }

    public RepositorySource getRepositorySource() {
        return this.repositorySource;
    }

    public void discoverExtensions(PackType packType) {
        Platform platform = Platform.getInstance();
        Path resourcePacksPath = ExtensionRegistry.getExtensionsPath();
        this.extensions = ExtensionRegistry.scanExtensions(resourcePacksPath, packType == PackType.CLIENT_RESOURCES);
        Extension builtInExtension = Extension.fromBuiltIn(packType == PackType.CLIENT_RESOURCES);
        if (builtInExtension != null && !builtInExtension.getItemBuilders().isEmpty()) {
            this.extensions.add(0, builtInExtension);
        }
        ArrayList<2> extensionPacks = new ArrayList<2>();
        for (final Extension extension : this.extensions) {
            if (extension.isBuiltIn()) continue;
            LOGGER.error("Adding extension: " + String.valueOf(extension));
            extensionPacks.add(new PathPackResources(this, extension.packLocationInfo, extension.path){
                private final JarFileSystem jarFileSystem;
                private static final Set<String> namespaces = Collections.singleton("pointblank");
                {
                    super(arg0, arg1);
                    this.jarFileSystem = Platform.getInstance().getJarFileSystem(extension.path);
                }

                public Set<String> getNamespaces(PackType packType) {
                    return namespaces;
                }

                public IoSupplier<InputStream> getRootResource(String ... pathSegments) {
                    FileUtil.validatePath((String[])pathSegments);
                    Path path = this.jarFileSystem.getPath(String.join((CharSequence)"/", pathSegments), new String[0]);
                    return Files.exists(path, new LinkOption[0]) ? IoSupplier.create((Path)path) : null;
                }

                public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
                    IoSupplier<InputStream> ioSupplier = this.getResource2(type, location);
                    if (ioSupplier != null && (location.getPath().startsWith("recipe/") || location.getPath().startsWith("recipes/"))) {
                        return RecipeAdapter.adaptRecipe(ioSupplier);
                    }
                    return this.getResource2(type, location);
                }

                public IoSupplier<InputStream> getResource2(PackType type, ResourceLocation location) {
                    return this.getRootResource(2.getPathFromLocation(location.getPath().startsWith("lang/") ? PackType.CLIENT_RESOURCES : type, location));
                }

                private static String[] getPathFromLocation(PackType type, ResourceLocation location) {
                    String[] parts = location.getPath().split("/");
                    String[] result = new String[parts.length + 2];
                    result[0] = type.getDirectory();
                    result[1] = location.getNamespace();
                    System.arraycopy(parts, 0, result, 2, parts.length);
                    return result;
                }

                public void listResources(PackType type, String namespace, String path, PackResources.ResourceOutput resourceOutput) {
                    this.listResources2(type, namespace, path, resourceOutput);
                    if (path.equals("recipe")) {
                        PackResources.ResourceOutput outputAdapter = (rl, ioSupplier) -> resourceOutput.accept(rl, RecipeAdapter.adaptRecipe((IoSupplier<InputStream>)ioSupplier));
                        this.listResources2(type, namespace, "recipes", outputAdapter);
                    }
                }

                protected Path resolve(String ... paths) {
                    if (paths.length < 1) {
                        throw new IllegalArgumentException("Missing path");
                    }
                    return this.jarFileSystem.getPath(String.join((CharSequence)"/", paths), new String[0]);
                }

                public void listResources2(PackType packType, String namespace, String path, PackResources.ResourceOutput resourceOutput) {
                    FileUtil.decomposePath((String)path).ifSuccess(pathSegments -> 2.listPath((String)namespace, (Path)this.resolve(packType.getDirectory(), namespace).toAbsolutePath(), (List)pathSegments, (PackResources.ResourceOutput)resourceOutput)).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)path, (Object)error.message()));
                }
            });
        }
        ArrayList hiddenPacks = new ArrayList();
        int version = SharedConstants.getCurrentVersion().getPackVersion(packType);
        PackLocationInfo info = new PackLocationInfo("pointblank_resources", (Component)Component.translatable((String)"poinblank.resources", (Object[])new Object[]{hiddenPacks.size()}), PackSource.DEFAULT, Optional.empty());
        PackMetadataSection packMeta = new PackMetadataSection((Component)Component.translatable((String)"pointblank.resources.extension_resources", (Object[])new Object[]{extensionPacks.size()}), version, Optional.empty());
        PackResources delegating = platform.createDelegatingPackResources(info, packMeta, extensionPacks);
        Pack.ResourcesSupplier supplier = platform.createPackResourceSupplier(delegating);
        PackSelectionConfig packSelectionConfig = new PackSelectionConfig(true, Pack.Position.BOTTOM, false);
        this.extensionsPack = Pack.readMetaAndCreate((PackLocationInfo)info, (Pack.ResourcesSupplier)supplier, (PackType)packType, (PackSelectionConfig)packSelectionConfig);
    }

    public static void registerItemsFromExtensions(ExtensionRegistry registry) {
        AmmoUiRegistry.clear();
        HeatUiRegistry.reset();
        for (Extension extension : registry.getExtensions()) {
            ArrayList registeredExtItems = new ArrayList();
            ItemRegistry extItems = ItemRegistry.ITEMS;
            for (String string : extension.getSounds()) {
                Supplier<SoundEvent> registeredSound = SoundRegistry.register(string);
                extension.registeredExtSounds.put(string, registeredSound);
            }
            for (ItemBuilder itemBuilder : extension.getItemBuilders()) {
                Supplier registeredExtItem = extItems.register(itemBuilder);
                registeredExtItems.add(registeredExtItem);
            }
            for (EffectBuilder effectBuilder : extension.getEffectBuilders()) {
                EffectRegistry.register(effectBuilder.getName(), () -> effectBuilder);
            }
            for (AmmoUiDefinition ammoUiDefinition : extension.getAmmoUiDefinitions()) {
                AmmoUiRegistry.register(ammoUiDefinition);
            }
            for (HeatUiDefinition heatUiDefinition : extension.getHeatUiDefinitions()) {
                HeatUiRegistry.register(heatUiDefinition);
            }
            for (PlayerAnimationBuilder playerAnimationBuilder : extension.getPlayerAnimationBuilders()) {
                ThirdPersonAnimationRegistry.register(playerAnimationBuilder.getName(), playerAnimationBuilder.getReaderFactory());
            }
            if (extension.isBuiltIn()) continue;
            Supplier<ItemStack> tabIconSupplier = () -> {
                Optional<Supplier> firstGunItem;
                Supplier<? extends Item> supplier;
                Item item = null;
                if (extension.creativeTabIconItem != null && (supplier = ItemRegistry.ITEMS.getItemsByName().get(extension.creativeTabIconItem)) != null) {
                    item = supplier.get();
                }
                if (item == null && (item = (firstGunItem = registeredExtItems.stream().filter(ro -> ro.get() instanceof GunItem).findFirst()).isPresent() ? (Item)firstGunItem.get().get() : (Item)GunRegistry.M4A1.get()) == null) {
                    item = AmmoRegistry.AMMOCREATIVE.get();
                }
                return new ItemStack((ItemLike)(item != null ? item : Items.AIR));
            };
            RegistryService<CreativeModeTab> registryService = Platform.getInstance().getCreativeModeTabRegistry();
            registryService.register(extension.getName(), () -> CreativeModeTab.builder((CreativeModeTab.Row)CreativeModeTab.Row.TOP, (int)0).title((Component)Component.translatable((String)("itemGroup." + extension.getName() + ".items"))).icon(tabIconSupplier).displayItems((enabledFeatures, entries) -> {
                for (Supplier registeredExtensionItem : registeredExtItems) {
                    entries.accept((ItemLike)registeredExtensionItem.get());
                }
            }).build());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static List<Extension> scanExtensions(Path extensionsPath, boolean isClientSide) {
        ArrayList<Extension> extensions = new ArrayList<Extension>();
        try {
            Files.createDirectories(extensionsPath, new FileAttribute[0]);
        }
        catch (IOException e) {
            LOGGER.error("Failed to create extension directory {}", (Object)extensionsPath, (Object)e);
            return extensions;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extensionsPath);){
            for (Path entry : stream) {
                Extension extension;
                if (Files.isDirectory(entry, new LinkOption[0])) {
                    try {
                        extension = Extension.fromPath(entry, isClientSide);
                        if (extension == null) continue;
                        extensions.add(extension);
                        continue;
                    }
                    catch (Exception re) {
                        throw new InvalidExtensionException(entry, (Throwable)re);
                    }
                }
                if (!entry.toString().endsWith(".zip")) continue;
                try {
                    extension = Extension.fromZipPath(entry, isClientSide);
                    if (extension == null) continue;
                    extensions.add(extension);
                }
                catch (Exception re) {
                    throw new InvalidExtensionException(entry, (Throwable)re);
                    return extensions;
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to scan extension directory {}", (Object)extensionsPath, (Object)e);
        }
        return extensions;
    }

    private static Set<String> getSoundEventNames(Path pathToJson) {
        HashSet<String> soundEventNames = new HashSet<String>();
        try (BufferedReader reader = Files.newBufferedReader(pathToJson);){
            JsonElement element = (JsonElement)new Gson().fromJson((Reader)reader, JsonElement.class);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                for (String key : jsonObject.keySet()) {
                    soundEventNames.add(key);
                }
            } else {
                LOGGER.error("Content of the sounds file " + String.valueOf(pathToJson) + " is not an object");
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to load json file {}", (Object)pathToJson, (Object)e);
            throw new RuntimeException("Failed to load file " + String.valueOf(pathToJson) + ". Error: " + String.valueOf(e));
        }
        return soundEventNames;
    }

    private static Set<String> getSoundEventNamesFromZip(ZipFile zipFile, ZipEntry entry) {
        HashSet<String> soundEventNames = new HashSet<String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));){
            JsonObject jsonObject = (JsonObject)new Gson().fromJson((Reader)reader, JsonObject.class);
            for (String key : jsonObject.keySet()) {
                soundEventNames.add(key);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return soundEventNames;
    }

    public static class Extension {
        private static final Set<String> SUPPORTED_BUILT_IN_FEATURE_TYPES = Set.of("ACCURACY", "AIMING", "AMMOCOUNTER", "DAMAGE", "DYNAMICRPM", "DYNAMIC_RPM", "FIREMODE", "AMMOCAPACITY", "RELOAD", "MUZZLEFLASH", "OVERHEAT", "OVERHEATING", "PARTVISIBILITY", "PIP", "RECOIL", "RETICLE", "SHELLEJECTION", "SHELL_EJECTION", "SOUND", "SKIN");
        private String name;
        private List<String> items;
        private Path path;
        private String creativeTabIconItem;
        private transient List<ItemBuilder<?>> itemBuilders;
        private transient List<EffectBuilder<?, ?>> effectBuilders;
        private transient List<EntityBuilder<?, ?>> entityBuilders;
        private transient List<AmmoUiDefinition> ammoUiDefinitions;
        private transient List<HeatUiDefinition> heatUiDefinitions;
        private transient Set<String> sounds;
        private transient Map<String, Supplier<SoundEvent>> registeredExtSounds;
        private transient PackLocationInfo packLocationInfo;
        private transient List<PlayerAnimationBuilder> playerAnimationBuilders;

        public Extension(String name, Path path, String creativeTabIconItem) {
            this.name = name;
            this.path = path;
            this.creativeTabIconItem = creativeTabIconItem;
        }

        List<ItemBuilder<?>> getItemBuilders() {
            return Collections.unmodifiableList(this.itemBuilders);
        }

        List<EffectBuilder<?, ?>> getEffectBuilders() {
            return this.effectBuilders;
        }

        List<EntityBuilder<?, ?>> getEntityBuilders() {
            return this.entityBuilders;
        }

        List<AmmoUiDefinition> getAmmoUiDefinitions() {
            return Collections.unmodifiableList(this.ammoUiDefinitions);
        }

        List<HeatUiDefinition> getHeatUiDefinitions() {
            return Collections.unmodifiableList(this.heatUiDefinitions);
        }

        Set<String> getSounds() {
            return Collections.unmodifiableSet(this.sounds);
        }

        List<PlayerAnimationBuilder> getPlayerAnimationBuilders() {
            return this.playerAnimationBuilders;
        }

        String getName() {
            return this.name;
        }

        public String getCreativeTabIconItem() {
            return this.creativeTabIconItem;
        }

        boolean isBuiltIn() {
            return this.path == null;
        }

        static Extension fromPath(Path extPath, boolean isClientSide) {
            Extension extension;
            block54: {
                Path extDescriptorPath = extPath.resolve("ext.json");
                if (!Files.exists(extDescriptorPath, new LinkOption[0]) || !Files.isRegularFile(extDescriptorPath, new LinkOption[0])) {
                    return null;
                }
                BufferedReader reader = Files.newBufferedReader(extDescriptorPath);
                try {
                    Path path;
                    Path path2;
                    Path uiPath;
                    Path effectsPath;
                    Extension extension2 = (Extension)new Gson().fromJson((Reader)reader, Extension.class);
                    extension2.packLocationInfo = new PackLocationInfo(extension2.name, (Component)Component.translatable((String)extension2.name), PackSource.DEFAULT, Optional.empty());
                    extension2.path = extPath;
                    extension2.itemBuilders = new ArrayList();
                    extension2.effectBuilders = new ArrayList();
                    extension2.entityBuilders = new ArrayList();
                    extension2.ammoUiDefinitions = new ArrayList<AmmoUiDefinition>();
                    extension2.heatUiDefinitions = new ArrayList<HeatUiDefinition>();
                    extension2.registeredExtSounds = new HashMap<String, Supplier<SoundEvent>>();
                    extension2.playerAnimationBuilders = new ArrayList<PlayerAnimationBuilder>();
                    Path namespacePath = extPath.resolve("assets").resolve("pointblank");
                    Path itemsPath = namespacePath.resolve("items");
                    if (Files.exists(itemsPath, new LinkOption[0]) && Files.isDirectory(itemsPath, new LinkOption[0])) {
                        try (DirectoryStream<Path> itemFiles = Files.newDirectoryStream(itemsPath, "*.json");){
                            for (Path path3 : itemFiles) {
                                extension2.itemBuilders.add(ItemBuilder.fromPath(path3, isClientSide));
                            }
                        }
                    }
                    if (Files.exists(effectsPath = namespacePath.resolve("effects"), new LinkOption[0]) && Files.isDirectory(effectsPath, new LinkOption[0])) {
                        try (DirectoryStream<Path> effectFiles = Files.newDirectoryStream(effectsPath, "*.json");){
                            for (Path path4 : effectFiles) {
                                extension2.effectBuilders.add(EffectBuilder.fromPath(path4, isClientSide));
                            }
                        }
                    }
                    if (Files.exists(uiPath = namespacePath.resolve("ui"), new LinkOption[0]) && Files.isDirectory(uiPath, new LinkOption[0])) {
                        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(uiPath, "*.json");){
                            for (Path path5 : directoryStream) {
                                extension2.ammoUiDefinitions.add(AmmoUiDefinition.fromPath(path5));
                            }
                        }
                    }
                    if (Files.exists(path2 = uiPath.resolve("heat"), new LinkOption[0]) && Files.isDirectory(path2, new LinkOption[0])) {
                        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path2, "*.json");){
                            for (Path uiFile : directoryStream) {
                                extension2.heatUiDefinitions.add(HeatUiDefinition.fromPath(uiFile));
                            }
                        }
                    }
                    extension2.sounds = Files.exists(path = namespacePath.resolve("sounds.json"), new LinkOption[0]) && Files.isRegularFile(path, new LinkOption[0]) ? ExtensionRegistry.getSoundEventNames(path) : Collections.emptySet();
                    Path path6 = namespacePath.resolve("animations").resolve("player");
                    if (Files.exists(path6, new LinkOption[0]) && Files.isDirectory(path6, new LinkOption[0])) {
                        try (DirectoryStream<Path> playerAnimationFiles = Files.newDirectoryStream(path6, "*.animation.json");){
                            for (Path playerAnimationFile : playerAnimationFiles) {
                                extension2.playerAnimationBuilders.add(PlayerAnimationBuilder.fromPath(playerAnimationFile));
                            }
                        }
                    }
                    extension = extension2;
                    if (reader == null) break block54;
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
                        LOGGER.error("Failed to load extension from path {}. Error: {}", (Object)extPath, (Object)e);
                        return null;
                    }
                }
                reader.close();
            }
            return extension;
        }

        /*
         * Enabled aggressive exception aggregation
         */
        static Extension fromZipPath(Path zipPath, boolean isClientSide) {
            try (ZipFile zipFile = new ZipFile(zipPath.toFile());){
                Extension extension;
                ZipEntry extDescriptorEntry = zipFile.getEntry("ext.json");
                if (extDescriptorEntry == null) {
                    Extension extension2 = null;
                    return extension2;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(extDescriptorEntry)));){
                    Extension extension3 = (Extension)new Gson().fromJson((Reader)reader, Extension.class);
                    extension3.packLocationInfo = new PackLocationInfo(extension3.name, (Component)Component.translatable((String)extension3.name), PackSource.DEFAULT, Optional.empty());
                    extension3.path = zipPath;
                    extension3.itemBuilders = new ArrayList();
                    extension3.effectBuilders = new ArrayList();
                    extension3.entityBuilders = new ArrayList();
                    extension3.ammoUiDefinitions = new ArrayList<AmmoUiDefinition>();
                    extension3.heatUiDefinitions = new ArrayList<HeatUiDefinition>();
                    extension3.registeredExtSounds = new HashMap<String, Supplier<SoundEvent>>();
                    extension3.playerAnimationBuilders = new ArrayList<PlayerAnimationBuilder>();
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().startsWith("assets/pointblank/items/") && entry.getName().endsWith(".json")) {
                            extension3.itemBuilders.add(ItemBuilder.fromZipEntry(zipFile, entry, isClientSide));
                            continue;
                        }
                        if (entry.getName().startsWith("assets/pointblank/effects/") && entry.getName().endsWith(".json")) {
                            extension3.effectBuilders.add(EffectBuilder.fromZipEntry(zipFile, entry, isClientSide));
                            continue;
                        }
                        if (entry.getName().startsWith("assets/pointblank/ui/heat/") && entry.getName().endsWith(".json")) {
                            extension3.heatUiDefinitions.add(HeatUiDefinition.fromZipEntry(zipFile, entry));
                            continue;
                        }
                        if (entry.getName().startsWith("assets/pointblank/ui/") && entry.getName().endsWith(".json")) {
                            extension3.ammoUiDefinitions.add(AmmoUiDefinition.fromZipEntry(zipFile, entry));
                            continue;
                        }
                        if (entry.getName().startsWith("assets/pointblank/entities/") && entry.getName().endsWith(".json")) {
                            extension3.entityBuilders.add(EntityBuilder.fromZipEntry(zipFile, entry, isClientSide));
                            continue;
                        }
                        if (!entry.getName().startsWith("assets/pointblank/animations/player/") || !entry.getName().endsWith(".animation.json")) continue;
                        extension3.playerAnimationBuilders.add(PlayerAnimationBuilder.fromZipEntry(zipFile, entry));
                    }
                    ZipEntry soundsEntry = zipFile.getEntry("assets/pointblank/sounds.json");
                    if (soundsEntry != null) {
                        extension3.sounds = ExtensionRegistry.getSoundEventNamesFromZip(zipFile, soundsEntry);
                    }
                    extension = extension3;
                }
                return extension;
            }
            catch (IOException e) {
                LOGGER.error("Failed to load extension from ZIP {}. Error: {}", (Object)zipPath, (Object)e);
                return null;
            }
        }

        static Extension fromBuiltIn(boolean isClientSide) {
            String extJsonPath = "/ext.json";
            String itemsPath = "/data/pointblank/items/";
            try {
                Extension extension;
                InputStream extStream = Extension.class.getResourceAsStream("/ext.json");
                if (extStream == null) {
                    LOGGER.warn("Built-in ext.json not found at {}", (Object)"/ext.json");
                    return null;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(extStream));){
                    extension = (Extension)new Gson().fromJson((Reader)reader, Extension.class);
                }
                extension.path = null;
                extension.itemBuilders = new ArrayList();
                extension.effectBuilders = new ArrayList();
                extension.entityBuilders = new ArrayList();
                extension.ammoUiDefinitions = new ArrayList<AmmoUiDefinition>();
                extension.heatUiDefinitions = new ArrayList<HeatUiDefinition>();
                extension.sounds = Collections.emptySet();
                extension.registeredExtSounds = new HashMap<String, Supplier<SoundEvent>>();
                extension.playerAnimationBuilders = new ArrayList<PlayerAnimationBuilder>();
                if (extension.items == null || extension.items.isEmpty()) {
                    LOGGER.warn("Built-in ext.json has no items list");
                    return extension;
                }
                for (String fileName : extension.items) {
                    try {
                        InputStream itemStream = Extension.class.getResourceAsStream("/data/pointblank/items/" + fileName);
                        try {
                            if (itemStream == null) {
                                LOGGER.warn("Built-in item definition not found: {}", (Object)fileName);
                                continue;
                            }
                            extension.itemBuilders.add(Extension.readBuiltInItemBuilder(itemStream, fileName, isClientSide));
                        }
                        finally {
                            if (itemStream == null) continue;
                            itemStream.close();
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error("Failed to load built-in item definition: {}", (Object)fileName, (Object)e);
                    }
                }
                LOGGER.info("Loaded {} built-in item definitions", (Object)extension.itemBuilders.size());
                return extension;
            }
            catch (Exception e) {
                LOGGER.error("Failed to load built-in ext.json", (Throwable)e);
                return null;
            }
        }

        private static ItemBuilder<?> readBuiltInItemBuilder(InputStream itemStream, String fileName, boolean isClientSide) throws IOException {
            try (BufferedReader itemReader = new BufferedReader(new InputStreamReader(itemStream));){
                JsonObject itemObject = (JsonObject)new Gson().fromJson((Reader)itemReader, JsonObject.class);
                if (itemObject == null) {
                    throw new IllegalArgumentException("Item definition is empty");
                }
                JsonElement features = itemObject.get("features");
                if (features != null && features.isJsonArray()) {
                    JsonArray filteredFeatures = new JsonArray();
                    for (JsonElement featureElement : features.getAsJsonArray()) {
                        String featureType;
                        if (!featureElement.isJsonObject()) continue;
                        JsonObject featureObject = featureElement.getAsJsonObject();
                        String string = featureType = featureObject.has("type") ? featureObject.getAsJsonPrimitive("type").getAsString() : "";
                        if (SUPPORTED_BUILT_IN_FEATURE_TYPES.contains(featureType.toUpperCase(Locale.ROOT))) {
                            filteredFeatures.add((JsonElement)featureObject);
                            continue;
                        }
                        LOGGER.debug("Skipping unsupported built-in feature '{}' in {}", (Object)featureType, (Object)fileName);
                    }
                    itemObject.add("features", (JsonElement)filteredFeatures);
                }
                ItemBuilder<?> itemBuilder = ItemBuilder.fromReader(new StringReader(new Gson().toJson((JsonElement)itemObject)), isClientSide);
                return itemBuilder;
            }
        }
    }
}

