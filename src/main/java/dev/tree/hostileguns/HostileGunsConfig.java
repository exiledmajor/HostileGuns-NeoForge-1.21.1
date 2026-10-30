package dev.tree.hostileguns;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public final class HostileGunsConfig {
    private HostileGunsConfig() {}

    public static double armedHostileChance = 0.12D;
    public static double maxRange = 32.0D;
    public static float baseDamage = 5.0F;
    public static int minShotCooldownTicks = 10;
    public static int maxShotCooldownTicks = 18;
    public static boolean allowModdedHostiles = true;

    public static final Set<String> excludedEntities = new LinkedHashSet<>(Arrays.asList(
            "minecraft:ender_dragon","minecraft:wither","minecraft:warden",
            "minecraft:elder_guardian","minecraft:guardian","minecraft:ravager",
            "minecraft:creeper","minecraft:ghast","minecraft:phantom",
            "minecraft:slime","minecraft:magma_cube","minecraft:shulker",
            "minecraft:spider","minecraft:cave_spider","minecraft:silverfish",
            "minecraft:endermite","minecraft:blaze","minecraft:breeze",
            "minecraft:hoglin","minecraft:zoglin","minecraft:vex"
    ));

    public static void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("hostileguns.properties");
        Properties p = new Properties();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) { p.load(in); }
            catch (IOException ignored) {}
        }
        armedHostileChance = clamp(parseDouble(p,"armedHostileChance",armedHostileChance),0,1);
        maxRange = Math.max(4,parseDouble(p,"maxRange",maxRange));
        baseDamage = (float)Math.max(.5,parseDouble(p,"baseDamage",baseDamage));
        minShotCooldownTicks = Math.max(1,parseInt(p,"minShotCooldownTicks",minShotCooldownTicks));
        maxShotCooldownTicks = Math.max(minShotCooldownTicks,parseInt(p,"maxShotCooldownTicks",maxShotCooldownTicks));
        allowModdedHostiles = Boolean.parseBoolean(p.getProperty("allowModdedHostiles","true"));
        String excluded=p.getProperty("excludedEntities");
        if(excluded!=null){ excludedEntities.clear(); for(String id:excluded.split(",")){id=id.trim();if(!id.isEmpty())excludedEntities.add(id);} }
        p.setProperty("armedHostileChance",Double.toString(armedHostileChance));
        p.setProperty("maxRange",Double.toString(maxRange));
        p.setProperty("baseDamage",Float.toString(baseDamage));
        p.setProperty("minShotCooldownTicks",Integer.toString(minShotCooldownTicks));
        p.setProperty("maxShotCooldownTicks",Integer.toString(maxShotCooldownTicks));
        p.setProperty("allowModdedHostiles",Boolean.toString(allowModdedHostiles));
        p.setProperty("excludedEntities",String.join(",",excludedEntities));
        try {
            Files.createDirectories(path.getParent());
            try(OutputStream out=Files.newOutputStream(path)){
                p.store(out,"Hostile Guns for NeoForge 1.21.1\nDefault armedHostileChance = 0.12 (12%).");
            }
        } catch(IOException ignored){}
    }
    private static double parseDouble(Properties p,String k,double f){try{return Double.parseDouble(p.getProperty(k,Double.toString(f)));}catch(NumberFormatException e){return f;}}
    private static int parseInt(Properties p,String k,int f){try{return Integer.parseInt(p.getProperty(k,Integer.toString(f)));}catch(NumberFormatException e){return f;}}
    private static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
}
